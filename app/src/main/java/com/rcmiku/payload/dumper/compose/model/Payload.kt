package com.rcmiku.payload.dumper.compose.model

import chromeos_update_engine.UpdateMetadata

data class Payload(
    val fileName: String,
    val header: PayloadHeader,
    val deltaArchiveManifest: UpdateMetadata.DeltaArchiveManifest,
    val dataOffset: Long,
    val blockSize: Int,
    val archiveSize: Long,
    val isPath: Boolean
)

data class PayloadHeader(
    val fileFormatVersion: Long,
    val manifestSize: Long,
    val metadataSignatureSize: Int,
)

data class PartitionInfo(
    val partitionName: String,
    val size: Long,
    val rawSize: Long,
    val sha256: String,
    val isDownloading: Boolean = false,
    val progress: Float = 0f,
)

data class FileInfo(
    val offset: Long,
    val size: Long
)

data class ArchiveInfo(
    val fileName: String,
    val fileSize: Long,
    val securityPatchLevel: String
)