package com.rcmiku.payload.dumper.compose.utils

import chromeos_update_engine.UpdateMetadata
import com.rcmiku.payload.dumper.compose.model.PartitionInfo
import com.rcmiku.payload.dumper.compose.model.Payload
import com.rcmiku.payload.dumper.compose.model.PayloadHeader
import com.rcmiku.payload.dumper.compose.utils.MiscUtil.toBufferedInputStream
import com.rcmiku.payload.dumper.compose.utils.MiscUtil.toHexString
import com.rcmiku.payload.dumper.compose.utils.MiscUtil.toInt
import com.rcmiku.payload.dumper.compose.utils.MiscUtil.toLong
import com.rcmiku.payload.dumper.compose.utils.ZipFileUtil.locateCentralDirectory
import com.rcmiku.payload.dumper.compose.utils.ZipFileUtil.locateLocalFileHeader
import com.rcmiku.payload.dumper.compose.utils.ZipFileUtil.locateLocalFileOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.apache.commons.compress.compressors.CompressorInputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import org.apache.commons.io.IOUtils
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.channels.Channels
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

object PayloadUtil {

    private const val MAGIC_VALUE = "CrAU"
    private const val FORMAT_VERSION = 2L

    private val muteX by lazy {
        Mutex()
    }

    private fun initPayloadLocal(
        fileName: String,
        raf: RandomAccessFile,
        payloadOffset: Long
    ): Payload {
        raf.seek(payloadOffset)
        val magicBytes = ByteArray(4)

        raf.readFully(magicBytes)
        if (String(magicBytes, StandardCharsets.UTF_8) != MAGIC_VALUE) {
            throw RuntimeException("Invalid magic value")
        }

        val fileFormatVersion = raf.readLong()
        if (fileFormatVersion != FORMAT_VERSION) {
            throw RuntimeException("Unsupported file format version")
        }

        val manifestSize = raf.readLong()
        val metadataSignatureSize = raf.readInt()

        val manifest = ByteArray(manifestSize.toInt())
        raf.readFully(manifest)

        val metadataSignatureMessage = ByteArray(metadataSignatureSize)
        raf.readFully(metadataSignatureMessage)

        val deltaArchiveManifest = UpdateMetadata.DeltaArchiveManifest.parseFrom(manifest)
        val dataOffset: Long = raf.channel.position()
        val payloadHeader = PayloadHeader(fileFormatVersion, manifestSize, metadataSignatureSize)
        return Payload(
            fileName,
            payloadHeader,
            deltaArchiveManifest,
            dataOffset,
            deltaArchiveManifest.blockSize,
            raf.length(),
            true
        )
    }

    private suspend fun initPayloadFromHttp(
        fileName: String,
        httpUtil: HttpUtil,
        payloadOffset: Long
    ): Payload {
        httpUtil.seek(payloadOffset)
        val magicBytes = ByteArray(4)
        httpUtil.read(magicBytes)
        if (String(magicBytes, StandardCharsets.UTF_8) != MAGIC_VALUE) {
            throw IOException("Invalid magic value")
        }

        val fileFormatVersionBytes = ByteArray(8)
        httpUtil.read(fileFormatVersionBytes)
        val fileFormatVersion = fileFormatVersionBytes.toLong()
        if (fileFormatVersion != FORMAT_VERSION) {
            throw IOException("Unsupported file format version")
        }

        val manifestSizeBytes = ByteArray(8)
        httpUtil.read(manifestSizeBytes)
        val manifestSize = manifestSizeBytes.toLong()

        val metadataSignatureSizeBytes = ByteArray(4)
        httpUtil.read(metadataSignatureSizeBytes)
        val metadataSignatureSize = metadataSignatureSizeBytes.toInt()

        val manifest = ByteArray(manifestSize.toInt())
        httpUtil.read(manifest)
        val metadataSignatureMessage = ByteArray(metadataSignatureSize)
        httpUtil.read(metadataSignatureMessage)
        val deltaArchiveManifest = UpdateMetadata.DeltaArchiveManifest.parseFrom(manifest)
        val dataOffset: Long = httpUtil.position()
        val payloadHeader = PayloadHeader(fileFormatVersion, manifestSize, metadataSignatureSize)
        return Payload(
            fileName,
            payloadHeader,
            deltaArchiveManifest,
            dataOffset,
            deltaArchiveManifest.blockSize,
            httpUtil.length(),
            false
        )
    }

    suspend fun initPayload(fileName: String, input: Any, payloadOffset: Long): Payload {
        if (payloadOffset == -1L) {
            throw IOException("Invalid payload offset value")
        }

        return when (input) {
            is RandomAccessFile -> {
                initPayloadLocal(fileName, input, payloadOffset)
            }

            is HttpUtil -> {
                initPayloadFromHttp(fileName, input, payloadOffset)
            }

            else -> throw IllegalArgumentException("Unsupported input type: ${input.javaClass.name}.")
        }
    }

    fun getPartitionInfoList(payload: Payload): List<PartitionInfo> {
        return payload.deltaArchiveManifest.partitionsList.map {
            PartitionInfo(
                it.partitionName,
                it.newPartitionInfo.size,
                (it.operationsList[it.operationsList.size - 1].dataOffset + it.operationsList[it.operationsList.size - 1].dataLength) - it.operationsList[0].dataOffset,
                it.newPartitionInfo.hash.toHexString()
            )
        }
    }

    private suspend fun extractFromLocal(
        op: UpdateMetadata.InstallOperation,
        partOutput: RandomAccessFile,
        raf: RandomAccessFile,
        blockSize: Int,
        offset: Long,
    ) {
        muteX.withLock {
            raf.seek(offset + op.dataOffset)
            partOutput.seek(op.dstExtentsList[0].startBlock * blockSize)

            val otaInputStream = Channels.newInputStream(raf.channel)
            val copyCompressedData: (CompressorInputStream) -> Unit = { compressorInputStream ->
                IOUtils.copy(compressorInputStream, FileOutputStream(partOutput.fd))
            }

            when (op.type) {
                UpdateMetadata.InstallOperation.Type.REPLACE_XZ -> {
                    copyCompressedData(XZCompressorInputStream(otaInputStream))
                }

                UpdateMetadata.InstallOperation.Type.REPLACE_BZ -> {
                    copyCompressedData(
                        BZip2CompressorInputStream(
                            BufferedInputStream(otaInputStream)
                        )
                    )
                }

                UpdateMetadata.InstallOperation.Type.REPLACE -> {
                    val data = ByteArray(op.dataLength.toInt())
                    raf.readFully(data)
                    partOutput.write(data)
                }

                UpdateMetadata.InstallOperation.Type.ZERO -> {
                    val data = ByteArray(op.dataLength.toInt()) { 0x00 }
                    partOutput.write(data)
                }

                else -> {
                    throw RuntimeException("Unsupported operation type ${op.type}")
                }
            }
        }
    }

    private suspend fun extractFromHttp(
        op: UpdateMetadata.InstallOperation,
        partOutput: RandomAccessFile,
        httpUtil: HttpUtil,
        blockSize: Int,
        offset: Long,
    ) {
        muteX.withLock {
            httpUtil.seek(offset + op.dataOffset)
            withContext(Dispatchers.IO) {
                partOutput.seek(op.dstExtentsList[0].startBlock * blockSize)
            }

            val copyCompressedData: (CompressorInputStream) -> Unit = { compressorInputStream ->
                IOUtils.copy(compressorInputStream, FileOutputStream(partOutput.fd))
            }

            when (op.type) {
                UpdateMetadata.InstallOperation.Type.REPLACE_XZ -> {
                    val data = ByteArray(op.dataLength.toInt())
                    httpUtil.read(data)
                    copyCompressedData(XZCompressorInputStream(data.toBufferedInputStream()))
                }

                UpdateMetadata.InstallOperation.Type.REPLACE_BZ -> {
                    val data = ByteArray(op.dataLength.toInt())
                    httpUtil.read(data)
                    copyCompressedData(
                        BZip2CompressorInputStream(
                            BufferedInputStream(data.toBufferedInputStream())
                        )
                    )
                }

                UpdateMetadata.InstallOperation.Type.REPLACE -> {
                    val data = ByteArray(op.dataLength.toInt())
                    httpUtil.read(data)
                    withContext(Dispatchers.IO) {
                        partOutput.write(data)
                    }

                }

                UpdateMetadata.InstallOperation.Type.ZERO -> {
                    val data = ByteArray(op.dataLength.toInt()) { 0x00 }
                    withContext(Dispatchers.IO) {
                        partOutput.write(data)
                    }
                }

                else -> {
                    throw RuntimeException("Unsupported operation type ${op.type}")
                }
            }
        }
    }

    suspend fun extractPartition(
        metadataPartition: UpdateMetadata.PartitionUpdate,
        input: Any,
        outputDir: String,
        payload: Payload,
        onProgressUpdate: (Long) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val downloadDir = Paths.get(outputDir)
            if (!Files.exists(downloadDir)) {
                Files.createDirectories(downloadDir)
            }

            RandomAccessFile(
                "$outputDir/${metadataPartition.partitionName}.img",
                "rw"
            ).use { partOutput ->
                metadataPartition.operationsList.forEach { operation ->
                    when (input) {
                        is RandomAccessFile -> {
                            extractFromLocal(
                                operation,
                                partOutput,
                                input,
                                payload.blockSize,
                                payload.dataOffset,
                            )
                        }

                        is HttpUtil -> {
                            extractFromHttp(
                                operation,
                                partOutput,
                                input,
                                payload.blockSize,
                                payload.dataOffset
                            )
                        }
                    }
                    onProgressUpdate((partOutput.channel.position()))
                }
            }
        }
    }

    suspend fun getPayloadOffset(pathOrUrl: String): Long {
        var payloadOffset = -1L
        val endBytes = ByteArray(4096)
        val fileName = "payload.bin"
        if (pathOrUrl.endsWith(".bin")) {
            payloadOffset = 0
        } else if (!pathOrUrl.startsWith("https://")) {
            val zipFile = File(pathOrUrl)
            withContext(Dispatchers.IO) {
                RandomAccessFile(zipFile, "r").use {
                    it.seek(it.length() - 4096)
                    it.readFully(endBytes)
                    val centralDirectoryInfo = locateCentralDirectory(endBytes, it.length())
                    it.seek(centralDirectoryInfo.offset)
                    val centralDirectory = ByteArray(centralDirectoryInfo.size.toInt())
                    it.readFully(centralDirectory)
                    val localHeaderOffset = locateLocalFileHeader(centralDirectory, fileName)
                    val localHeaderBytes = ByteArray(256)
                    it.seek(localHeaderOffset)
                    it.readFully(localHeaderBytes)
                    payloadOffset = locateLocalFileOffset(localHeaderBytes) + localHeaderOffset
                }
            }
        } else if (pathOrUrl.startsWith("https://")) {
            HttpUtil.seek(HttpUtil.length() - 4096)
            HttpUtil.read(endBytes)
            val centralDirectoryInfo = locateCentralDirectory(endBytes, HttpUtil.length())
            HttpUtil.seek(centralDirectoryInfo.offset)
            val centralDirectory = ByteArray(centralDirectoryInfo.size.toInt())
            HttpUtil.read(centralDirectory)
            val localHeaderOffset = locateLocalFileHeader(centralDirectory, fileName)
            val localHeaderBytes = ByteArray(256)
            HttpUtil.seek(localHeaderOffset)
            HttpUtil.read(localHeaderBytes)
            payloadOffset = locateLocalFileOffset(localHeaderBytes) + localHeaderOffset
        }
        return payloadOffset
    }
}
