package com.rcmiku.payload.dumper.compose.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rcmiku.payload.dumper.compose.R
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.utils.MiuixPopupUtil.Companion.dismissDialog

@Composable
fun InputDialog(
    showDialog: MutableState<Boolean>,
    url: MutableState<String>
) {
    val input = rememberSaveable { mutableStateOf("") }
    SuperDialog(
        title = stringResource(R.string.ota_url),
        show = showDialog,
        onDismissRequest = {
            dismissDialog(showDialog)
        },
    ) {
        TextField(
            modifier = Modifier.padding(bottom = 16.dp),
            value = input.value,
            minLines = 2,
            onValueChange = { input.value = it }
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                text = stringResource(R.string.clear),
                onClick = {
                    input.value = ""
                },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(20.dp))
            TextButton(
                text = stringResource(R.string.confirm),
                onClick = {
                    if (input.value.isNotBlank()) {
                        dismissDialog(showDialog)
                        url.value = input.value
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.textButtonColorsPrimary()
            )
        }
    }
}