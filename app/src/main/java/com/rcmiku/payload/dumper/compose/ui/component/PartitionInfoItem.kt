package com.rcmiku.payload.dumper.compose.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rcmiku.payload.dumper.compose.R
import com.rcmiku.payload.dumper.compose.model.PartitionInfo
import com.rcmiku.payload.dumper.compose.utils.MiscUtil.sizeIn
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun PartitionInfoItem(partitionInfo: PartitionInfo, onClick: () -> Unit, onDownload: () -> Unit) {

    Row(
        modifier = Modifier
            .clickable(
                onClick = {
                    onClick()
                }
            )
            .padding(horizontal = 12.dp)
            .fillMaxWidth()
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.ic_img),
            contentDescription = partitionInfo.partitionName,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = partitionInfo.partitionName,
                style = MiuixTheme.textStyles.title4,
            )
            Text(
                text = partitionInfo.size.sizeIn(),
                style = MiuixTheme.textStyles.subtitle,
                color = MiuixTheme.colorScheme.onSurfaceContainerVariant
            )
        }
        if (partitionInfo.isDownloading && partitionInfo.progress < 1f) {
            Box(
                modifier = Modifier
                    .height(30.dp)
                    .width(60.dp)
                    .align(Alignment.CenterVertically)
            ) {
                CircularProgressIndicator(
                    progress = { partitionInfo.progress },
                    modifier = Modifier
                        .size(30.dp)
                        .align(Alignment.Center),
                    color = MiuixTheme.colorScheme.primary,
                    trackColor = MiuixTheme.colorScheme.tertiaryContainerVariant,
                )
            }
        } else if (!partitionInfo.isDownloading) {
            Button(
                modifier = Modifier
                    .height(30.dp)
                    .width(60.dp)
                    .align(Alignment.CenterVertically),
                colors = ButtonDefaults.buttonColors(containerColor = MiuixTheme.colorScheme.tertiaryContainerVariant),
                contentPadding = PaddingValues(0.dp),
                onClick = {
                    onDownload()
                },
            ) {
                Text(
                    text = stringResource(R.string.dump),
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Badge(
                modifier = Modifier
                    .height(30.dp)
                    .width(60.dp)
                    .align(Alignment.CenterVertically),
                containerColor = MiuixTheme.colorScheme.onPrimaryVariant
            ) {
                Text(
                    text = stringResource(R.string.done),
                    style = MiuixTheme.textStyles.subtitle,
                    color = MiuixTheme.colorScheme.primaryVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}