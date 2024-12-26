package com.rcmiku.payload.dumper.compose.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import com.rcmiku.payload.dumper.compose.utils.PreferencesUtil
import com.rcmiku.payload.dumper.compose.utils.UserAgentUtil
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.extra.SuperSwitch
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.MiuixPopupUtil.Companion.dismissDialog

@Composable
fun CustomUserAgent(
    showDialog: MutableState<Boolean>,
) {
    val isCustomUserAgentEnabled = rememberSaveable {
        mutableStateOf(
            PreferencesUtil().perfGetBoolean("isCustomUserAgentEnabled") ?: false
        )
    }

    val customUserAgent = rememberSaveable {
        mutableStateOf(
            PreferencesUtil().perfGet("customUserAgent") ?: UserAgentUtil.DEFAULT_USER_AGENT
        )
    }

    SuperDialog(
        title = stringResource(R.string.custom_user_agent_setting),
        show = showDialog,
        onDismissRequest = {
            dismissDialog(showDialog)
        },
    ) {
        Card(
            modifier = Modifier.padding(bottom = 12.dp),
            color = MiuixTheme.colorScheme.secondaryContainer,
        ) {
            SuperSwitch(
                title = stringResource(R.string.custom_user_agent),
                checked = isCustomUserAgentEnabled.value,
                onCheckedChange = {
                    isCustomUserAgentEnabled.value = it
                    PreferencesUtil().perfSet("isCustomUserAgentEnabled", it)
                },
            )
        }
        AnimatedVisibility(
            visible = isCustomUserAgentEnabled.value,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            TextField(
                modifier = Modifier
                    .padding(bottom = 16.dp),
                value = customUserAgent.value,
                onValueChange = { customUserAgent.value = it }
            )
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                text = stringResource(R.string.cancel),
                onClick = {
                    dismissDialog(showDialog)
                },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(20.dp))
            TextButton(
                text = stringResource(R.string.confirm),
                onClick = {
                    dismissDialog(showDialog)
                    val userAgent = customUserAgent.value.ifBlank {
                        UserAgentUtil.DEFAULT_USER_AGENT.also { customUserAgent.value = it }
                    }
                    PreferencesUtil().perfSet("customUserAgent", userAgent)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.textButtonColorsPrimary()
            )
        }
    }
}

