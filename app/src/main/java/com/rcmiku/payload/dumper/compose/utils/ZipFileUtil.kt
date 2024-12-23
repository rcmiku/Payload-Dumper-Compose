package com.rcmiku.payload.dumper.compose.utils

import com.rcmiku.payload.dumper.compose.model.FileInfo
import java.nio.ByteBuffer
import java.nio.ByteOrder

object ZipFileUtil {

    private const val CENSIG = 0x02014b50L   // "PK\001\002"
    private const val LOCSIG = 0x04034b50L   // "PK\003\004"
    private const val ENDSIG = 0x06054b50L // "PK\005\006"
    private const val ENDHDR = 22
    private const val ZIP64_ENDSIG = 0x06064b50L  // "PK\006\006"
    private const val ZIP64_LOCSIG = 0x07064b50L  // "PK\006\007"
    private const val ZIP64_LOCHDR = 20
    private const val ZIP64_MAGICVAL = 0xFFFFFFFFL

    fun locateCentralDirectory(byteArray: ByteArray, fileLength: Long): FileInfo {
        val byteBuffer = ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN)
        val offset = byteBuffer.capacity() - ENDHDR
        var cenSize: Long = -1
        var cenOffset: Long = -1

        for (i in 0..byteBuffer.capacity() - ENDHDR) {
            byteBuffer.position(offset - i)
            if (byteBuffer.getInt().toLong() == ENDSIG) {
                val endSigOffset = byteBuffer.position()
                byteBuffer.position(byteBuffer.position() + 12)

                if (byteBuffer.getInt().toUInt().toLong() == ZIP64_MAGICVAL) {
                    byteBuffer.position(endSigOffset - ZIP64_LOCHDR - 4)

                    if (byteBuffer.getInt().toLong() == ZIP64_LOCSIG) {
                        byteBuffer.position(byteBuffer.position() + 4)
                        val zip64EndSigOffset = byteBuffer.getLong()
                        byteBuffer.position(4096 - (fileLength - zip64EndSigOffset).toInt())

                        if (byteBuffer.getInt().toLong() == ZIP64_ENDSIG) {
                            byteBuffer.position(byteBuffer.position() + 36)
                            cenSize = byteBuffer.getLong().toULong().toLong()
                            cenOffset = byteBuffer.getLong().toULong().toLong()
                        }
                    }
                } else {
                    byteBuffer.position(endSigOffset + 8)
                    cenSize = byteBuffer.getInt().toUInt().toLong()
                    cenOffset = byteBuffer.getInt().toUInt().toLong()
                    break
                }
            }
        }

        return FileInfo(cenOffset, cenSize)
    }

    fun locateLocalFileHeader(byteArray: ByteArray, fileName: String): Long {
        val byteBuffer = ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN)
        var localHeaderOffset: Long = -1

        while (true) {
            if (byteBuffer.getInt().toLong() == CENSIG) {
                byteBuffer.position(byteBuffer.position() + 24)
                val fileNameLength = byteBuffer.getShort().toUInt().toInt()
                val extraFieldLength = byteBuffer.getShort().toUInt().toInt()
                val fileCommentLength = byteBuffer.getShort().toUInt().toInt()
                byteBuffer.position(byteBuffer.position() + 8)
                val localHeaderOffsetTemp = byteBuffer.getInt().toUInt().toLong()
                val fileNameBytes = ByteArray(fileNameLength)
                byteBuffer.get(fileNameBytes)
                if (fileName == String(fileNameBytes, Charsets.UTF_8)) {
                    localHeaderOffset = localHeaderOffsetTemp
                    break
                }
                byteBuffer.position(byteBuffer.position() + extraFieldLength + fileCommentLength)
            } else {
                break
            }
        }

        return localHeaderOffset
    }

    fun locateLocalFileOffset(byteArray: ByteArray): Long {
        val byteBuffer = ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN)
        var localFileOffset: Long = -1

        if (byteBuffer.getInt().toLong() == LOCSIG) {
            byteBuffer.position(byteBuffer.position() + 22)
            val fileNameLength = byteBuffer.getShort().toUInt().toInt()
            val extraFieldLength = byteBuffer.getShort().toUInt().toInt()
            byteBuffer.position(byteBuffer.position() + fileNameLength + extraFieldLength)
            localFileOffset = byteBuffer.position().toLong()
        }

        return localFileOffset
    }
}