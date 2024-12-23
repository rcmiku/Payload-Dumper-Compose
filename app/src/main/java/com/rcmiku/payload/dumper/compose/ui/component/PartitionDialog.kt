package com.rcmiku.payload.dumper.compose.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import com.rcmiku.payload.dumper.compose.R
import com.rcmiku.payload.dumper.compose.model.PartitionInfo
import com.rcmiku.payload.dumper.compose.utils.MiscUtil.sizeIn
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.utils.MiuixPopupUtil.Companion.dismissDialog

@Composable
fun PartitionDialog(showDialog: MutableState<Boolean>, partitionInfo: PartitionInfo) {

    SuperDialog(
        show = showDialog,
        title = stringResource(R.string.image_info),
        onDismissRequest = {
            dismissDialog(showDialog)
        }
    ) {
        InfoTextView(stringResource(R.string.image_name), partitionInfo.partitionName)
        InfoTextView(stringResource(R.string.image_size), partitionInfo.size.sizeIn())
        InfoTextView(stringResource(R.string.raw_size), partitionInfo.rawSize.sizeIn())
        InfoTextView(stringResource(R.string.image_sha256), partitionInfo.sha256)
    }
}