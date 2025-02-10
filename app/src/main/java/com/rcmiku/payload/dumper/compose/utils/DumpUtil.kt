package com.rcmiku.payload.dumper.compose.utils

import android.os.Environment
import android.widget.Toast
import com.rcmiku.payload.dumper.compose.R
import com.rcmiku.payload.dumper.compose.model.Payload
import com.rcmiku.payload.dumper.compose.utils.AppContextUtil.context

class DumpUtil {

    suspend fun dump(
        partitionName: String,
        payload: Payload,
        isPath: Boolean,
        onProgressUpdate: (Long) -> Unit,
        onFailure: (Boolean) -> Unit,
    ) {
        Toast.makeText(
            context,
            context.getString(R.string.start_dump_message),
            Toast.LENGTH_SHORT
        ).show()

        val appName = context.getString(R.string.app_name)
        val fileName = payload.fileName.removeSuffix(".zip")
        val dumpDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val customPath = PreferencesUtil().perfGet("customFolder")?.let {
            "$it/$fileName"
        }
        val outputDir = customPath ?: (dumpDir.path + "/${appName}/${fileName}")
        val dumpPath = dumpDir.name + "/" + appName + "/" + fileName

        kotlin.runCatching {
            payload.deltaArchiveManifest.partitionsList?.forEach {
                if (it.partitionName == partitionName)
                    if (dumpDir != null) {
                        if (!isPath) {
                            PayloadUtil.extractPartition(
                                it,
                                HttpUtil,
                                outputDir,
                                payload,
                                onProgressUpdate
                            )
                        } else {
                            PayloadUtil.extractPartition(
                                it,
                                RandomAccessFileUtil.raf,
                                outputDir,
                                payload,
                                onProgressUpdate
                            )
                        }
                    }
            }
        }.onSuccess {
            Toast.makeText(
                context,
                context.getString(
                    R.string.dump_success_message,
                    dumpPath
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