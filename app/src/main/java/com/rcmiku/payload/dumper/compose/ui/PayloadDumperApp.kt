package com.rcmiku.payload.dumper.compose.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.rcmiku.payload.dumper.compose.R
import com.rcmiku.payload.dumper.compose.model.PartitionInfo
import com.rcmiku.payload.dumper.compose.ui.component.AboutDialog
import com.rcmiku.payload.dumper.compose.ui.component.InputDialog
import com.rcmiku.payload.dumper.compose.ui.component.InputInfoCard
import com.rcmiku.payload.dumper.compose.ui.component.PartitionDialog
import com.rcmiku.payload.dumper.compose.ui.component.PartitionInfoItem
import com.rcmiku.payload.dumper.compose.ui.component.RomInfoCard
import com.rcmiku.payload.dumper.compose.utils.AppContextUtil
import com.rcmiku.payload.dumper.compose.utils.DumpUtil
import com.rcmiku.payload.dumper.compose.utils.MiscUtil.getRealPathFromUri
import com.rcmiku.payload.dumper.compose.utils.ParseUtil
import com.rcmiku.payload.dumper.compose.utils.PreferencesUtil
import com.rcmiku.payload.dumper.compose.viewModel.PayloadDumperViewModel
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Box
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.LazyColumn
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.extra.SuperArrow
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
    val pathOrUrl =
        rememberSaveable { mutableStateOf(PreferencesUtil().perfGet("pathOrUrl") ?: "") }
    val partitionInfoList = viewModel.partitionInfoList.collectAsState()
    val archiveInfo = viewModel.archiveInfo.collectAsState()
    val payload = viewModel.payload.collectAsState()
    val selectedPartitionInfo = remember { mutableStateOf<PartitionInfo?>(null) }
    val showDialog = remember { mutableStateOf(false) }
    val showInputDialog = remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                pathOrUrl.value =
                    getRealPathFromUri(context = AppContextUtil.context, uri).toString()
                PreferencesUtil().perfSet("pathOrUrl", pathOrUrl.value)
            }
        }
    )


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
                    Card(
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        SuperArrow(
                            leftAction = {
                                Box(
                                    contentAlignment = Alignment.TopStart,
                                    modifier = Modifier.padding(end = 16.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_local),
                                        contentDescription = "local",
                                        tint = Color.Unspecified
                                    )
                                }
                            },
                            title = stringResource(R.string.from_local),
                            onClick = {
                                launcher.launch("application/zip")
                            }
                        )
                        SuperArrow(
                            leftAction = {
                                Box(
                                    contentAlignment = Alignment.TopStart,
                                    modifier = Modifier.padding(end = 16.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_url),
                                        contentDescription = "url",
                                        tint = Color.Unspecified
                                    )
                                }
                            },
                            title = stringResource(R.string.from_url),
                            onClick = {
                                showInputDialog.value = true
                            }
                        )
                    }

                    if (pathOrUrl.value.isNotEmpty())
                        InputInfoCard(pathOrUrl = pathOrUrl.value)

                    Row(
                        modifier = Modifier
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            modifier = Modifier.weight(1f),
                            text = stringResource(R.string.parse),
                            onClick = {
                                coroutineScope.launch {
                                    ParseUtil().parse(pathOrUrl = pathOrUrl.value,
                                        onSuccess = { payloadTemp ->
                                            viewModel.initPayloadDumper(payload = payloadTemp)
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                        })
                                }
                            },
                            colors = ButtonDefaults.textButtonColorsPrimary()
                        )
                    }
                    archiveInfo.value?.let { RomInfoCard(archiveInfo = it) }
                }
                if (partitionInfoList.value.isNotEmpty()) {
                    SmallTitle(stringResource(R.string.image_list))
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 12.dp),
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
                                        DumpUtil().dump(
                                            partitionName = partitionInfo.partitionName,
                                            payload = payload.value!!,
                                            isPath = payload.value!!.isPath,
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
    }
    InputDialog(showInputDialog, pathOrUrl)
    selectedPartitionInfo.value?.let { PartitionDialog(showDialog, it) }
}

