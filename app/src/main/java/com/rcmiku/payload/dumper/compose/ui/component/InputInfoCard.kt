package com.rcmiku.payload.dumper.compose.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rcmiku.payload.dumper.compose.R
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun InputInfoCard(pathOrUrl: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        insideMargin = PaddingValues(horizontal = 12.dp)
    ) {
        Text(
            text = stringResource(R.string.input),
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .padding(top = 12.dp)
                .padding(bottom = 16.dp)
        )
        Text(
            text = pathOrUrl,
            style = MiuixTheme.textStyles.body1,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .padding(bottom = 12.dp)
        )
    }
}