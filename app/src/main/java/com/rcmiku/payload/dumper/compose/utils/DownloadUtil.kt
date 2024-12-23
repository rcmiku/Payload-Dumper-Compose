package com.rcmiku.payload.dumper.compose.utils

import android.os.Environment
import android.widget.Toast
import com.rcmiku.payload.dumper.compose.R
import com.rcmiku.payload.dumper.compose.model.Payload
import com.rcmiku.payload.dumper.compose.utils.AppContextUtil.context


class DownloadUtil {

    suspend fun download(
        partitionName: String,
        payload: Payload,
        onProgressUpdate: (Long) -> Unit,
        onFailure: (Boolean) -> Unit,
    ) {
        Toast.makeText(
            context,
            context.getString(R.string.start_download_message),
            Toast.LENGTH_SHORT
        ).show()

        val appName = context.getString(R.string.app_name)
        val fileName = payload.fileName.removeSuffix(".zip")
        val downloadDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val outputDir = downloadDir.path + "/${appName}/${fileName}"
        val downloadPath = downloadDir.name + "/" + appName + "/" + fileName

        kotlin.runCatching {
            payload.deltaArchiveManifest.partitionsList?.forEach {
                if (it.partitionName == partitionName)
                    if (downloadDir != null) {
                        PayloadUtil.extractPartition(
                            it,
                            HttpUtil,
                            outputDir,
                            payload,
                            onProgressUpdate
                        )
                    }
            }
        }.onSuccess {
            Toast.makeText(
                context,
                context.getString(
                    R.string.download_success_message,
                    downloadPath
                ),
                Toast.LENGTH_SHORT
            ).show()
        }.onFailure { error ->
            Toast.makeText(
                context,
                error.message,
                Toast.LENGTH_SHORT
            ).show()
            onFailure(false)
        }
    }
}