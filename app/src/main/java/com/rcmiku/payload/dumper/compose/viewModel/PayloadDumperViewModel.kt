package com.rcmiku.payload.dumper.compose.viewModel

import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rcmiku.payload.dumper.compose.model.ArchiveInfo
import com.rcmiku.payload.dumper.compose.model.PartitionInfo
import com.rcmiku.payload.dumper.compose.model.Payload
import com.rcmiku.payload.dumper.compose.utils.PayloadUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PayloadDumperViewModel : ViewModel() {

    private val _partitionInfoList = MutableStateFlow<List<PartitionInfo>>(emptyList())
    val partitionInfoList: StateFlow<List<PartitionInfo>> = _partitionInfoList
    private var _archiveInfo = MutableStateFlow<ArchiveInfo?>(null)
    val archiveInfo: StateFlow<ArchiveInfo?> = _archiveInfo
    private var _payload = MutableStateFlow<Payload?>(null)
    val payload: StateFlow<Payload?> = _payload

    fun initPayloadDumper(payload: Payload) {
        viewModelScope.launch {
            _payload.value = payload
            _archiveInfo.value = ArchiveInfo(
                payload.fileName,
                payload.archiveSize,
                payload.deltaArchiveManifest.securityPatchLevel
            )
            _partitionInfoList.value = PayloadUtil.getPartitionInfoList(payload)
        }
    }

    fun updateProgress(index: Int, progress: Float) {
        _partitionInfoList.value.let { currentList ->
            if (index in currentList.indices) {
                val updatedList = currentList.toMutableStateList()
                updatedList[index] = updatedList[index].copy(progress = progress)
                _partitionInfoList.value = updatedList
            }
        }
    }

    fun updateDownloadState(index: Int, isDownloading: Boolean) {
        _partitionInfoList.value.let { currentList ->
            if (index in currentList.indices) {
                val updatedList = currentList.toMutableStateList()
                updatedList[index] = updatedList[index].copy(isDownloading = isDownloading)
                _partitionInfoList.value = updatedList
            }
        }
    }

}