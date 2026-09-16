package com.example.inscit.transfer

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class TransferStage {
    SE_IDLE, ADVERTISING, DISCOVERING, CONNECTING, TRANSFERRING, SUCCESS, DENIED
}

data class TransferState(
    val stage: TransferStage = TransferStage.SE_IDLE,
    val endpointName: String? = null,
    val endpointId: String? = null,
    val pairCode: String? = null,
    val bytesTransferred: Long = 0L,
    val totalBytes: Long = 0L,
    val error: String? = null
)

class TransferViewModel : ViewModel() {
    private val _state = MutableStateFlow(TransferState())
    val state: StateFlow<TransferState> = _state.asStateFlow()

    fun setStage(stage: TransferStage) {
        _state.value = _state.value.copy(stage = stage)
    }

    fun setPairCode(code: String) {
        _state.value = _state.value.copy(pairCode = code)
    }

    fun setEndpoint(id: String, name: String) {
        _state.value = _state.value.copy(endpointId = id, endpointName = name)
    }

    fun setProgress(transferred: Long, total: Long) {
        _state.value = _state.value.copy(bytesTransferred = transferred, totalBytes = total, stage = TransferStage.TRANSFERRING)
    }

    fun setError(msg: String) {
        _state.value = _state.value.copy(error = msg)
    }

    fun reset() {
        _state.value = TransferState()
    }
}
