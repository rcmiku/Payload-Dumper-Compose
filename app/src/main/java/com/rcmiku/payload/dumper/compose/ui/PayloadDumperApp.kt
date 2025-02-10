package com.rcmiku.payload.dumper.compose.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.rcmiku.payload.dumper.compose.R
import com.rcmiku.payload.dumper.compose.model.PartitionInfo
import com.rcmiku.payload.dumper.compose.ui.component.AboutDialog
import com.rcmiku.payload.dumper.compose.ui.component.CustomUserAgent
import com.rcmiku.payload.dumper.compose.ui.component.InputDialog
import com.rcmiku.payload.dumper.compose.ui.component.InputInfoCard
import com.rcmiku.payload.dumper.compose.ui.component.PartitionDialog
import com.rcmiku.payload.dumper.compose.ui.component.PartitionInfoItem
import com.rcmiku.payload.dumper.compose.ui.component.RequestPermissionDialog
import com.rcmiku.payload.dumper.compose.ui.component.RomInfoCard
import com.rcmiku.payload.dumper.compose.utils.AppContextUtil
import com.rcmiku.payload.dumper.compose.utils.DumpUtil
import com.rcmiku.payload.dumper.compose.utils.MiscUtil.getItemShape
import com.rcmiku.payload.dumper.compose.utils.MiscUtil.getRealPathFromUri
import com.rcmiku.payload.dumper.compose.utils.MiscUtil.isManageExternalStoragePermissionGranted
import com.rcmiku.payload.dumper.compose.utils.ParseUtil
import com.rcmiku.payload.dumper.compose.utils.PreferencesUtil
import com.rcmiku.payload.dumper.compose.viewModel.PayloadDumperViewModel
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeEffectScope
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.LazyColumn
import top.yukonga.miuix.kmp.basic.ListPopup
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.ListPopupDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SearchBar
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.extra.DropdownImpl
import top.yukonga.miuix.kmp.extra.SuperArrow
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.ImmersionMore
import top.yukonga.miuix.kmp.icon.icons.Search
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.MiuixPopupUtil.Companion.dismissPopup

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
    val partitionInfoList by viewModel.cachePartitionInfoList.collectAsState()
    val archiveInfo = viewModel.archiveInfo.collectAsState()
    val payload = viewModel.payload.collectAsState()
    val selectedPartitionInfo = remember { mutableStateOf<PartitionInfo?>(null) }
    val showDialog = remember { mutableStateOf(false) }
    val showInputDialog = remember { mutableStateOf(false) }
    val showCustomUserAgentDialog = remember { mutableStateOf(false) }
    val showAboutDialog = remember { mutableStateOf(false) }
    val showRequestPermissionDialog = remember { mutableStateOf(false) }
    var expanded by rememberSaveable { mutableStateOf(false) }
    val showTopPopup = rememberSaveable { mutableStateOf(false) }
    val isTopPopupExpanded = rememberSaveable { mutableStateOf(false) }
    val dropdownOptions = stringArrayResource(R.array.dropdownOptions)
    val searchValue = viewModel.search.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                getRealPathFromUri(context = AppContextUtil.context, uri)?.let { path ->
                    pathOrUrl.value = path
                    PreferencesUtil().perfSet("pathOrUrl", path)
                }
            }
        }
    )

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            getRealPathFromUri(context = AppContextUtil.context, uri)?.let { path ->
                PreferencesUtil().perfSet(
                    "customFolder",
                    path
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                color = Color.Transparent,
                modifier = Modifier
                    .hazeEffect(state = hazeState,
                        style = hazeStyleTopAppBar,
                        block = fun HazeEffectScope.() {
                            inputScale = HazeInputScale.Auto
                        }),
                title = stringResource(R.string.app_name),
                actions = {
                    if (isTopPopupExpanded.value) {
                        ListPopup(
                            show = showTopPopup,
                            popupPositionProvider = ListPopupDefaults.ContextMenuPositionProvider,
                            alignment = PopupPositionProvider.Align.TopRight,
                            onDismissRequest = {
                                isTopPopupExpanded.value = false
                            }
                        ) {
                            ListPopupColumn {
                                dropdownOptions.forEachIndexed { index, text ->
                                    DropdownImpl(
                                        text = text,
                                        optionSize = dropdownOptions.size,
                                        isSelected = false,
                                        onSelectedIndexChange = {
                                            dismissPopup(showTopPopup)
                                            isTopPopupExpanded.value = false
                                            when (index) {
                                                0 -> showCustomUserAgentDialog.value = true
                                                1 -> {
                                                    if (isManageExternalStoragePermissionGranted())
                                                        folderPickerLauncher.launch(null)
                                                    else
                                                        showRequestPermissionDialog.value = true
                                                }

                                                2 -> showAboutDialog.value = true
                                            }
                                        },
                                        index = index
                                    )
                                }
                            }
                        }
                        showTopPopup.value = true
                    }
                    IconButton(
                        modifier = Modifier
                            .padding(end = 18.dp)
                            .size(40.dp),
                        onClick = {
                            isTopPopupExpanded.value = true
                        }
                    ) {
                        Icon(
                            imageVector = MiuixIcons.ImmersionMore,
                            contentDescription = "Menu"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .hazeSource(state = hazeState),
            topAppBarScrollBehavior = scrollBehavior,
            contentPadding = padding
        ) {
            item {
                SearchBar(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 6.dp),
                    inputField = {
                        InputField(
                            query = searchValue.value,
                            onQueryChange = {
                                viewModel.updateSearch(it)
                            },
                            onSearch = {
                                keyboardController?.hide()
                            },
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            label = stringResource(R.string.search),
                            leadingIcon = {
                                Icon(
                                    modifier = Modifier.padding(
                                        start = 12.dp,
                                        end = 8.dp,
                                        top = 14.dp,
                                        bottom = 14.dp
                                    ),
                                    imageVector = MiuixIcons.Search,
                                    tint = MiuixTheme.colorScheme.onSurfaceContainer,
                                    contentDescription = "Search"
                                )
                            },
                        )
                    },
                    outsideRightAction = {
                        Text(
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .clickable(
                                    interactionSource = null,
                                    indication = null
                                ) {
                                    expanded = false
                                    viewModel.updateSearch("")
                                },
                            text = stringResource(R.string.cancel),
                            color = MiuixTheme.colorScheme.primary
                        )
                    },
                    expanded = expanded,
                    onExpandedChange = {
                        expanded = it
                    }
                ) {
                }
                AnimatedVisibility(
                    visible = !expanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
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
                                    if (isManageExternalStoragePermissionGranted())
                                        launcher.launch("application/zip")
                                    else
                                        showRequestPermissionDialog.value = true
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
                                        ParseUtil().parse(
                                            pathOrUrl = pathOrUrl.value,
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
                }
                if (partitionInfoList.isNotEmpty())
                    SmallTitle(stringResource(R.string.image_list))
            }

            itemsIndexed(partitionInfoList) { index, partitionInfo ->
                val shape = getItemShape(
                    prevItem = partitionInfoList.getOrNull(index - 1),
                    nextItem = partitionInfoList.getOrNull(index + 1),
                    corner = 16.dp,
                    subCorner = 4.dp
                )
                androidx.compose.material3.Card(
                    shape = shape,
                    modifier = Modifier
                        .heightIn(min = 64.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 4.dp),
                    colors = CardDefaults.cardColors()
                        .copy(containerColor = MiuixTheme.colorScheme.surface)
                )
                {
                    PartitionInfoItem(
                        partitionInfo = partitionInfo,
                        onClick = {
                            selectedPartitionInfo.value = partitionInfo
                            showDialog.value = true
                        },
                        onDownload = {
                            viewModel.updateDownloadState(
                                partitionName = partitionInfo.partitionName,
                                true
                            )
                            coroutineScope.launch {
                                DumpUtil().dump(
                                    partitionName = partitionInfo.partitionName,
                                    payload = payload.value!!,
                                    isPath = payload.value!!.isPath,
                                    onProgressUpdate = { progress ->
                                        viewModel.updateProgress(
                                            partitionName = partitionInfo.partitionName,
                                            progress = progress / partitionInfo.size.toFloat()
                                        )
                                    }, onFailure = { isDownload ->
                                        viewModel.updateDownloadState(
                                            partitionName = partitionInfo.partitionName,
                                            isDownload
                                        )
                                    })
                            }
                        })
                }
            }
        }
    }
    InputDialog(showInputDialog, pathOrUrl)
    CustomUserAgent(showCustomUserAgentDialog)
    AboutDialog(showAboutDialog)
    RequestPermissionDialog(showRequestPermissionDialog)
    selectedPartitionInfo.value?.let { PartitionDialog(showDialog, it) }
}