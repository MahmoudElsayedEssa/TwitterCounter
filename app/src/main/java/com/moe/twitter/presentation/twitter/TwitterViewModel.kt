package com.moe.twitter.presentation.twitter

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.TextLayoutResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moe.twitter.data.remote.auth.OAuthManager
import com.moe.twitter.domain.model.TweetPublishException
import com.moe.twitter.domain.usecase.CheckTextIssuesUseCase
import com.moe.twitter.domain.usecase.ComputeTweetMetricsUseCase
import com.moe.twitter.domain.usecase.PostTweetUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TwitterViewModel(
    private val postTweetUseCase: PostTweetUseCase,
    private val checkTextIssuesUseCase: CheckTextIssuesUseCase,
    private val computeTweetMetricsUseCase: ComputeTweetMetricsUseCase,
    private val oauthManager: OAuthManager
) : ViewModel() {

    private val _state = MutableStateFlow<TwitterState>(TwitterState.Content())
    val state = _state.asStateFlow()

    private val _effects = MutableSharedFlow<TwitterEffect>(extraBufferCapacity = 4)
    val effects: SharedFlow<TwitterEffect> = _effects.asSharedFlow()

    private val _ghostEvents = MutableSharedFlow<GhostEvent>(extraBufferCapacity = 64)
    val ghostEvents: SharedFlow<GhostEvent> = _ghostEvents.asSharedFlow()

    private var checkJob: Job? = null
    private var lastLayout: TextLayoutResult? = null
    private var previousText: String = ""
    private var authFlowStarted = false

    init {
        val isAuthed = oauthManager.isAuthenticated()
        updateContent { it.copy(isAuthenticated = isAuthed) }
        if (!isAuthed) {
            startAuthIfNeeded()
        }
    }

    fun refreshAuthState() {
        updateContent { it.copy(isAuthenticated = oauthManager.isAuthenticated()) }
    }

    fun onAction(action: TwitterAction) {
        when (action) {
            is TwitterAction.OnTextChange -> handleTextChange(action.value)
            is TwitterAction.OnClear -> handleClear()
            is TwitterAction.OnCopy -> handleCopy()
            is TwitterAction.OnPost -> handlePost()
            is TwitterAction.OnTextLayout -> lastLayout = action.layout
            is TwitterAction.OnLogin -> { /* no-op: auto login handled */ }
            is TwitterAction.OnLogout -> handleClear()
        }
    }

    private fun handleTextChange(newValue: String) {
        viewModelScope.launch {

            updateContent {
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
            val text = (_state.value as TwitterState.Content).text
            if (layout != null && text.isNotEmpty()) {
                spawnClearAllGhosts(
                    text = text,
                    layout = layout
                )
            }

            updateContent {
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
            val text = (_state.value as TwitterState.Content).text
            if (text.isEmpty()) {
                _effects.emit(TwitterEffect.ShowToast("Nothing to copy"))
                return@launch
            }
            _effects.emit(TwitterEffect.CopyToClipboard(text))
            _effects.emit(TwitterEffect.ShowToast("Copied"))
        }
    }

    private fun handlePost() {
        viewModelScope.launch {
            val current = (_state.value as TwitterState.Content).text

            // Validation
            if (current.isBlank()) {
                _effects.emit(TwitterEffect.ShowToast("Cannot post empty text"))
                return@launch
            }
            if (!(_state.value as TwitterState.Content).metrics.withinLimit) {
                _effects.emit(TwitterEffect.ShowToast("Text exceeds 280 characters"))
                return@launch
            }

            // Set posting state atomically
            updateContent { it.copy(postingState = PostingState.Posting) }

            val result = postTweetUseCase(current)
            result.fold(
                onSuccess = {
                    updateContent { it.copy(postingState = PostingState.Success) }
                    _effects.emit(TwitterEffect.ShowToast("Posted!"))

                    delay(1800)
                    handleClear()
                    updateContent { it.copy(postingState = PostingState.Idle) }
                },
                onFailure = { error ->
                    val rawMessage = error.message ?: "Post failed"
                    val displayMessage = if (error is TweetPublishException && rawMessage.equals("No client available", ignoreCase = true)) {
                        "NO CLIENT!"
                    } else {
                        rawMessage
                    }
                    updateContent { it.copy(postingState = PostingState.Error(displayMessage)) }
                    _effects.emit(TwitterEffect.ShowToast(displayMessage))

                    delay(2000)
                    updateContent { it.copy(postingState = PostingState.Idle) }
                }
            )
        }
    }

    private fun startAuthIfNeeded() {
        if (authFlowStarted) return
        authFlowStarted = true
        oauthManager.startAuthFlow()
    }

    private fun launchDebouncedCheck(text: String) {
        checkJob?.cancel()
        if (text.isBlank() || text.length < 5) {
            updateContent { it.copy(errors = emptyList(), isChecking = false) }
            return
        }

        checkJob = viewModelScope.launch {
            updateContent { it.copy(isChecking = true) }
            delay(600)
            if (!isActive) return@launch

            val issues = checkTextIssuesUseCase(text).getOrElse { emptyList() }
            updateContent { it.copy(errors = issues, isChecking = false) }
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
            _ghostEvents.emit(GhostEvent.Backspace(seed, delayMs = stepIndex * 18L))
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
            _ghostEvents.emit(GhostEvent.Clear(seeds = seeds, smartDelayMs = smartDelay))
        }
    }

    private inline fun updateContent(block: (TwitterState.Content) -> TwitterState.Content) {
        _state.update { state ->
            when (state) {
                is TwitterState.Content -> block(state)
            }
        }
    }
}


