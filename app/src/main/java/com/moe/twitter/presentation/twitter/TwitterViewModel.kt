package com.moe.twitter.presentation.twitter

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.TextLayoutResult
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

    private val _ghostEvents = Channel<GhostEvent>(Channel.BUFFERED)
    val ghostEvents = _ghostEvents.receiveAsFlow()

    private var checkJob: Job? = null
    private var lastLayout: TextLayoutResult? = null
    private var previousText: String = ""

    fun onAction(action: TwitterAction) {
        when (action) {
            is TwitterAction.OnTextChange -> handleTextChange(action.value)
            is TwitterAction.OnClear -> handleClear()
            is TwitterAction.OnCopy -> handleCopy()
            is TwitterAction.OnPost -> handlePost()
            is TwitterAction.OnTextLayout -> lastLayout = action.layout
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

            val layout = lastLayout
            if (layout != null) {
                handleDeletions(
                    oldText = previousText,
                    newText = newValue,
                    layout = layout
                )
            }
            previousText = newValue
        }
    }

    private fun handleClear() {
        viewModelScope.launch {
            val layout = lastLayout
            val text = _state.value.text
            if (layout != null && text.isNotEmpty()) {
                spawnClearAllGhosts(
                    text = text,
                    layout = layout
                )
            }

            _state.update {
                it.copy(
                    text = "",
                    metrics = computeTweetMetricsUseCase(""),
                    errors = emptyList(),
                    isChecking = false,
                    clearSignal = it.clearSignal + 1
                )
            }
            checkJob?.cancel()
            previousText = ""
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
            if (!_state.value.metrics.withinLimit) {
                _effects.send(TwitterEffect.ShowToast("Text exceeds 280 characters"))
                return@launch
            }

            _state.update { it.copy(isPosting = true) }
            when (val result = postTweetUseCase(current)) {
                PostTweetResult.Success -> {
                    _effects.send(TwitterEffect.ShowToast("Posted!"))
                    _state.update { it.copy(logoAnimationTrigger = it.logoAnimationTrigger + 1) }
                    delay(1800)
                    handleClear()
                }

                PostTweetResult.NoClientAvailable -> {
//                    _effects.send(TwitterEffect.ShowToast("No network"))

                    _effects.send(TwitterEffect.ShowToast("Posted!"))
                    _state.update { it.copy(logoAnimationTrigger = it.logoAnimationTrigger + 1) }
                    delay(1800)
                    handleClear()

                }

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

    // ---------------- Ghost seeds (UI animates) ----------------

    private fun handleDeletions(
        oldText: String,
        newText: String,
        layout: TextLayoutResult
    ) {
        if (newText.length >= oldText.length) return

        val deletedCount = oldText.length - newText.length
        val startIndex = newText.length

        for (i in 0 until deletedCount) {
            val charIndex = startIndex + i
            if (charIndex >= oldText.length) break

            val ch = oldText[charIndex]
            val box = layout.getBoundingBox(charIndex)

            spawnBackspaceGhost(
                char = ch,
                box = box,
                stepIndex = i
            )
        }
    }

    private fun spawnBackspaceGhost(
        char: Char,
        box: Rect,
        stepIndex: Int
    ) {
        val seed = GhostSeed(
            id = System.nanoTime(),
            char = char,
            baseX = box.left,
            baseY = box.top,
            order = stepIndex
        )
        viewModelScope.launch {
            _ghostEvents.send(GhostEvent.Backspace(seed, delayMs = stepIndex * 18L))
        }
    }

    private fun spawnClearAllGhosts(
        text: String,
        layout: TextLayoutResult
    ) {
        val total = text.length
        if (total == 0) return

        val indices = (0 until total).toList().reversed()
        val smartDelay = when {
            total > 30 -> 3L
            total > 20 -> 5L
            total > 10 -> 8L
            else -> 12L
        }

        val seeds = indices.mapIndexed { order, charIndex ->
            val ch = text[charIndex]
            val box = layout.getBoundingBox(charIndex)
            GhostSeed(
                id = System.nanoTime(),
                char = ch,
                baseX = box.left,
                baseY = box.top,
                order = order
            )
        }

        viewModelScope.launch {
            _ghostEvents.send(GhostEvent.Clear(seeds = seeds, smartDelayMs = smartDelay))
        }
    }
}


