package de.justkile.jlberlin.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.justkile.jlberlinmodel.District
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class TimerViewModel : ViewModel() {

    private val _time = MutableStateFlow(0)
    val time = _time.asStateFlow()
    
    private val _isClaiming = MutableStateFlow(false)
    val isClaiming: StateFlow<Boolean> = _isClaiming.asStateFlow()
    
    private val _districtBeingClaimed = MutableStateFlow<District?>(null)
    val districtBeingClaimed: StateFlow<District?> = _districtBeingClaimed.asStateFlow()

    private var timerJob: Job? = null

    fun startTimer(district: District) {
        _districtBeingClaimed.value = district
        _isClaiming.value = true
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1.seconds)
                _time.value++
            }
        }
    }

    fun stopTimer() {
        _time.value = 0
        _isClaiming.value = false
        _districtBeingClaimed.value = null
        timerJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }

}