package com.rcmiku.payload.dumper.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rcmiku.payload.dumper.compose.model.ArchiveInfo
import com.rcmiku.payload.dumper.compose.model.PartitionInfo
import com.rcmiku.payload.dumper.compose.model.Payload
import com.rcmiku.payload.dumper.compose.utils.PayloadUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class PayloadDumperViewModel : ViewModel() {

    private val _partitionInfoList = MutableStateFlow<List<PartitionInfo>>(emptyList())
    private val _cachePartitionInfoList = MutableStateFlow<List<PartitionInfo>>(emptyList())
    val cachePartitionInfoList: StateFlow<List<PartitionInfo>> = _cachePartitionInfoList
    private var _archiveInfo = MutableStateFlow<ArchiveInfo?>(null)
    val archiveInfo: StateFlow<ArchiveInfo?> = _archiveInfo
    private var _payload = MutableStateFlow<Payload?>(null)
    val payload: StateFlow<Payload?> = _payload
    private val _search = MutableStateFlow("")
    val search: StateFlow<String> = _search

    fun initPayloadDumper(payload: Payload) {
        viewModelScope.launch {
            _payload.value = payload
            _archiveInfo.value = ArchiveInfo(
                payload.fileName,
                payload.archiveSize,
                payload.deltaArchiveManifest.securityPatchLevel
            )
            _partitionInfoList.value = PayloadUtil.getPartitionInfoList(payload)
            _cachePartitionInfoList.value = _partitionInfoList.value
        }
    }

    fun updateProgress(partitionName: String, progress: Float) {
        _partitionInfoList.value = _partitionInfoList.value.map {
            if (it.partitionName == partitionName) {
                it.copy(progress = progress)
            } else {
                it
            }
        }
    }

    fun updateDownloadState(partitionName: String, isDownloading: Boolean) {
        _partitionInfoList.value = _partitionInfoList.value.map {
            if (it.partitionName == partitionName) {
                it.copy(isDownloading = isDownloading)
            } else {
                it
            }
        }
    }

    fun updateSearch(partitionName: String) {
        _search.value = partitionName
    }

    private fun updateBySearch() {
        viewModelScope.launch {
            combine(_partitionInfoList, _search) { partitionList, search ->
                partitionList.filter {
                    if (search.isNotEmpty()) it.partitionName.contains(search) else true
                }
            }.collect { filterList ->
                _cachePartitionInfoList.value = filterList
            }
        }
    }

    init {
        updateBySearch()
    }
}