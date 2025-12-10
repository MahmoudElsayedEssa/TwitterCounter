package com.moe.twitter.presentation.twitter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moe.twitter.domain.model.PostTweetResult
import com.moe.twitter.domain.usecase.CheckTextIssuesUseCase
import com.moe.twitter.domain.usecase.ComputeTweetMetricsUseCase
import com.moe.twitter.domain.usecase.PostTweetUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TwitterViewModel(
    private val postTweetUseCase: PostTweetUseCase,
    private val checkTextIssuesUseCase: CheckTextIssuesUseCase,
    private val computeTweetMetricsUseCase: ComputeTweetMetricsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TwitterState())
    val state = _state.asStateFlow()

    private val _effects = Channel<TwitterEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var checkJob: Job? = null

    fun onAction(action: TwitterAction) {
        when (action) {
            is TwitterAction.OnTextChange -> handleTextChange(action.value)
            is TwitterAction.OnClear -> handleClear()
            is TwitterAction.OnCopy -> handleCopy()
            is TwitterAction.OnPost -> handlePost()
        }
    }

    private fun handleTextChange(newValue: String) {
        viewModelScope.launch {

            _state.update {
                it.copy(
                    text = newValue,
                    metrics = computeTweetMetricsUseCase(newValue)
                )
            }
            launchDebouncedCheck(newValue)
        }
    }

    private fun handleClear() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    text = "",
                    metrics = computeTweetMetricsUseCase(""),
                    errors = emptyList(),
                    ghosts = emptyList(),
                    isChecking = false,
                    clearSignal = it.clearSignal + 1
                )
            }
            checkJob?.cancel()
        }
    }

    private fun handleCopy() {
        viewModelScope.launch {
            _effects.send(TwitterEffect.ShowToast("Copied"))
        }
    }

    private fun handlePost() {
        viewModelScope.launch {
            val current = _state.value.text
            if (current.isBlank()) {
                _effects.send(TwitterEffect.ShowToast("Cannot post empty text"))
                return@launch
            }

            _state.update { it.copy(isPosting = true) }
            when (val result = postTweetUseCase(current)) {
                PostTweetResult.Success -> _effects.send(TwitterEffect.ShowToast("Posted!"))
                PostTweetResult.NoClientAvailable -> _effects.send(TwitterEffect.ShowToast("No network"))
                is PostTweetResult.Failure -> _effects.send(
                    TwitterEffect.ShowToast(result.message ?: "Post failed")
                )
            }
            _state.update { it.copy(isPosting = false) }
        }
    }

    private fun launchDebouncedCheck(text: String) {
        checkJob?.cancel()
        if (text.isBlank() || text.length < 5) {
            _state.update { it.copy(errors = emptyList(), isChecking = false) }
            return
        }

        checkJob = viewModelScope.launch {
            _state.update { it.copy(isChecking = true) }
            delay(600)
            if (!isActive) return@launch

            val result = try {
                checkTextIssuesUseCase(text)
            } catch (_: Exception) {
                emptyList()
            }
            _state.update { it.copy(errors = result, isChecking = false) }
        }
    }
}


