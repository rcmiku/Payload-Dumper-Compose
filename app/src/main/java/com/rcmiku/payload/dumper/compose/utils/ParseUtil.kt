package com.rcmiku.payload.dumper.compose.utils

import android.widget.Toast
import com.rcmiku.payload.dumper.compose.R
import com.rcmiku.payload.dumper.compose.model.Payload
import com.rcmiku.payload.dumper.compose.utils.AppContextUtil.context
import com.rcmiku.payload.dumper.compose.utils.MiscUtil.isReadable
import com.rcmiku.payload.dumper.compose.utils.MiscUtil.isValidUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile

class ParseUtil {
    lateinit var payload: Payload

    suspend fun parse(pathOrUrl: String, onSuccess: (Payload) -> Unit) {
        if (pathOrUrl.isNotBlank() && pathOrUrl.isValidUrl()) {
            handleParsing(pathOrUrl, onSuccess) {
                HttpUtil.init(pathOrUrl)
                val payloadOffset = PayloadUtil.getPayloadOffset(pathOrUrl)
                PayloadUtil.initPayload(HttpUtil.getFileName(), HttpUtil, payloadOffset)
            }
        } else if (pathOrUrl.isReadable()) {
            handleParsing(pathOrUrl, onSuccess) {
                val payloadOffset = PayloadUtil.getPayloadOffset(pathOrUrl)
                val raf = withContext(Dispatchers.IO) { RandomAccessFile(pathOrUrl, "rw") }
                RandomAccessFileUtil.init(raf)
                val fileName = File(pathOrUrl).name
                PayloadUtil.initPayload(fileName, raf, payloadOffset)
            }
        } else {
            showToast(R.string.url_error_message)
        }
    }

    private suspend fun handleParsing(
        pathOrUrl: String,
        onSuccess: (Payload) -> Unit,
        parseLogic: suspend () -> Payload
    ) {
        showToast(R.string.parse_message)

        kotlin.runCatching {
            parseLogic()
        }.onSuccess { parsedPayload ->
            onSuccess(parsedPayload)
            savePreference(pathOrUrl)
            showToast(R.string.parse_success_message)
        }.onFailure { exception ->
            showToast(exception.message ?: "Unknown error")
        }
    }

    private fun showToast(messageResId: Int) {
        Toast.makeText(context, context.getString(messageResId), Toast.LENGTH_SHORT).show()
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun savePreference(pathOrUrl: String) {
        val key = if (pathOrUrl.isValidUrl()) "pathOrUrl" else "url"
        PreferencesUtil().perfSet(key, pathOrUrl)
    }

}