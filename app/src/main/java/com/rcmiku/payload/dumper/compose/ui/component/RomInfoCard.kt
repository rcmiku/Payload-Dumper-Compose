package com.rcmiku.payload.dumper.compose.ui.component

import android.text.TextUtils
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.rcmiku.payload.dumper.compose.R
import com.rcmiku.payload.dumper.compose.model.ArchiveInfo
import com.rcmiku.payload.dumper.compose.utils.MiscUtil.sizeIn
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun RomInfoCard(archiveInfo: ArchiveInfo) {
    Card(
        modifier = Modifier.padding(horizontal = 12.dp),
        insideMargin = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ic_zip),
                contentDescription = null,
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                EllipsisMiddleText(text = archiveInfo.fileName)
                Text(
                    text = archiveInfo.fileSize.sizeIn(),
                    style = MiuixTheme.textStyles.subtitle,
                    color = MiuixTheme.colorScheme.onSurfaceContainerVariant
                )
                Text(
                    text = archiveInfo.securityPatchLevel,
                    style = MiuixTheme.textStyles.subtitle,
                    color = MiuixTheme.colorScheme.onSurfaceContainerVariant
                )
            }
        }
    }
}

@Composable
fun EllipsisMiddleText(text: String) {
    val textColor = MiuixTheme.colorScheme.onBackground.toArgb()
    AndroidView(
        factory = { context ->
            TextView(context).apply {
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.MIDDLE
                textSize = 18.sp.value
                setTextColor(textColor)
            }
        },
        update = { it.text = text }
    )
}


