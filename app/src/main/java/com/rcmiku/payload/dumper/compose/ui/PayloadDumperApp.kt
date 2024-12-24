package com.rcmiku.payload.dumper.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.rcmiku.payload.dumper.compose.R
import com.rcmiku.payload.dumper.compose.model.PartitionInfo
import com.rcmiku.payload.dumper.compose.ui.component.AboutDialog
import com.rcmiku.payload.dumper.compose.ui.component.PartitionDialog
import com.rcmiku.payload.dumper.compose.ui.component.PartitionInfoItem
import com.rcmiku.payload.dumper.compose.ui.component.RomInfoCard
import com.rcmiku.payload.dumper.compose.utils.DownloadUtil
import com.rcmiku.payload.dumper.compose.utils.PreferencesUtil
import com.rcmiku.payload.dumper.compose.utils.SubmitUtil
import com.rcmiku.payload.dumper.compose.viewModel.PayloadDumperViewModel
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.LazyColumn
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(ExperimentalHazeApi::class)
@Composable
fun PayloadDumperApp(viewModel: PayloadDumperViewModel) {

    val scrollBehavior = MiuixScrollBehavior()
    val hazeState = remember { HazeState() }
    val hazeStyleTopAppBar = HazeStyle(
        blurRadius = 25.dp,
        backgroundColor = if (scrollBehavior.state.heightOffset > -1) Color.Transparent else MiuixTheme.colorScheme.background,
        tint = HazeTint(
            MiuixTheme.colorScheme.background.copy(
                if (scrollBehavior.state.heightOffset > -1) 1f
                else lerp(1f, 0.67f, (scrollBehavior.state.heightOffset + 1) / -143f)
            )
        )
    )
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val url = rememberSaveable { mutableStateOf(PreferencesUtil().perfGet("url") ?: "") }
    val partitionInfoList = viewModel.partitionInfoList.collectAsState()
    val archiveInfo = viewModel.archiveInfo.collectAsState()
    val payloadState = viewModel.payload.collectAsState()
    val selectedPartitionInfo = remember { mutableStateOf<PartitionInfo?>(null) }
    val showDialog = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                color = Color.Transparent,
                modifier = Modifier
                    .hazeChild(
                        state = hazeState,
                        style = hazeStyleTopAppBar
                    ) {
                        inputScale = HazeInputScale.Auto
                    },
                title = stringResource(R.string.app_name),
                actions = {
                    AboutDialog()
                },
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .haze(state = hazeState),
            topAppBarScrollBehavior = scrollBehavior,
            contentPadding = padding
        ) {
            item {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(top = 12.dp)
                ) {
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = url.value,
                        onValueChange = { url.value = it },
                        label = stringResource(R.string.ota_url),
                        backgroundColor = MiuixTheme.colorScheme.surface,
                        minLines = 2,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                            }
                        )
                    )
                    Row(
                        modifier = Modifier
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            modifier = Modifier.weight(1f),
                            text = stringResource(R.string.submit),
                            onClick = {
                                coroutineScope.launch {
                                    SubmitUtil().submit(url = url.value,
                                        onSuccess = { payload ->
                                            viewModel.initPayloadDumper(payload = payload)
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                        })
                                }
                            },
                            colors = ButtonDefaults.textButtonColorsPrimary()
                        )
                    }
                }
            }
            item {
                if (payloadState.value != null) {
                    Column(Modifier.padding(top = 12.dp)) {
                        archiveInfo.value?.let {
                            RomInfoCard(archiveInfo = it)
                        }
                    }
                }
            }
            item {
                if (partitionInfoList.value.isNotEmpty()) {
                    SmallTitle(stringResource(R.string.image_list))
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 12.dp)
                            .clip(
                                RoundedCornerShape(16.dp)
                            )
                            .background(color = MiuixTheme.colorScheme.surface)
                    ) {

                        partitionInfoList.value.forEachIndexed { index, partitionInfo ->
                            PartitionInfoItem(
                                partitionInfo = partitionInfo,
                                onClick = {
                                    selectedPartitionInfo.value = partitionInfo
                                    showDialog.value = true
                                },
                                onDownload = {
                                    viewModel.updateDownloadState(index = index, true)
                                    coroutineScope.launch {
                                        DownloadUtil().download(
                                            partitionName = partitionInfo.partitionName,
                                            payload = payloadState.value!!,
                                            onProgressUpdate = { progress ->
                                                viewModel.updateProgress(
                                                    index = index,
                                                    progress = progress / partitionInfo.size.toFloat()
                                                )
                                            }, onFailure = { isDownload ->
                                                viewModel.updateDownloadState(
                                                    index = index,
                                                    isDownload
                                                )
                                            })
                                    }
                                })
                        }
                    }
                }
            }
        }
        selectedPartitionInfo.value?.let { PartitionDialog(showDialog, it) }
    }
}

