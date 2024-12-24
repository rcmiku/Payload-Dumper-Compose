package com.rcmiku.payload.dumper.compose.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rcmiku.payload.dumper.compose.R
import com.rcmiku.payload.dumper.compose.model.ArchiveInfo
import com.rcmiku.payload.dumper.compose.utils.MiscUtil.sizeIn
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text

@Composable
fun RomInfoCard(archiveInfo: ArchiveInfo) {
    Card(
        modifier = Modifier.padding(horizontal = 12.dp),
        insideMargin = PaddingValues(horizontal = 12.dp)
    ) {
        Text(
            text = stringResource(R.string.ota_info),
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 12.dp).padding(bottom = 16.dp)
        )
        InfoTextView(stringResource(R.string.file_size),archiveInfo.fileSize.sizeIn())
        InfoTextView(stringResource(R.string.security_patch_level),archiveInfo.securityPatchLevel)
        InfoTextView(stringResource(R.string.file_name),archiveInfo.fileName)
    }
}


