package com.rcmiku.payload.dumper.compose.utils

import android.widget.Toast
import com.rcmiku.payload.dumper.compose.R
import com.rcmiku.payload.dumper.compose.model.Payload
import com.rcmiku.payload.dumper.compose.utils.AppContextUtil.context
import com.rcmiku.payload.dumper.compose.utils.MiscUtil.isValidUrl

class SubmitUtil {
    lateinit var payload: Payload

    suspend fun submit(url: String, onSuccess: (Payload) -> Unit) {
        if (url.isNotBlank() && url.isValidUrl()) {
            Toast.makeText(
                context,
                context.getString(R.string.submit_message),
                Toast.LENGTH_SHORT
            ).show()
            kotlin.runCatching {
                HttpUtil.init(url)
                val payloadOffset =
                    PayloadUtil.getPayloadOffset(url)
                payload =
                    PayloadUtil.initPayload(
                        HttpUtil.getFileName(),
                        HttpUtil,
                        payloadOffset
                    )
            }.onSuccess {
                onSuccess(payload)
                PreferencesUtil().perfSet("url", url)
                Toast.makeText(
                    context,
                    context.getString(R.string.submit_success_message),
                    Toast.LENGTH_SHORT
                ).show()
            }.onFailure { exception ->
                Toast.makeText(
                    context,
                    exception.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.url_error_message),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}