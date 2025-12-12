package com.moe.twitter.presentation.twitter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moe.twitter.data.remote.auth.OAuthManager
import com.moe.twitter.domain.TwitterConstants
import com.moe.twitter.domain.model.TweetMetrics
import com.moe.twitter.domain.model.TweetPublishException
import com.moe.twitter.domain.usecase.CheckTextIssuesUseCase
import com.moe.twitter.domain.usecase.ComputeTweetMetricsUseCase
import com.moe.twitter.domain.usecase.PostTweetUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class TwitterViewModel(
    private val postTweetUseCase: PostTweetUseCase,
    private val checkTextIssuesUseCase: CheckTextIssuesUseCase,
    private val computeTweetMetricsUseCase: ComputeTweetMetricsUseCase,
    private val oauthManager: OAuthManager
) : ViewModel() {

    companion object {
        private const val TEXT_VALIDATION_DEBOUNCE_MS = 600L
        private const val MIN_TEXT_LENGTH_FOR_CHECK = 5
        private const val POST_SUCCESS_DELAY_MS = 1800L
        private const val POST_ERROR_DELAY_MS = 2000L
    }

    private val _state = MutableStateFlow(TwitterState(
        isAuthenticated = oauthManager.isAuthenticated()
    ))
    val state = _state.asStateFlow()

    private val _effects = MutableSharedFlow<TwitterEffect>()
    val effects = _effects.asSharedFlow()

    private val textInput = MutableSharedFlow<String>(extraBufferCapacity = 1)

    init {
        if (!oauthManager.isAuthenticated()) {
            oauthManager.startAuthFlow()
        }
        observeTextValidation()
    }

    fun onAction(action: TwitterAction) {
        when (action) {
            is TwitterAction.OnTextChange -> handleTextChange(action.value)
            TwitterAction.OnClear -> handleClear()
            TwitterAction.OnCopy -> handleCopy()
            TwitterAction.OnPost -> handlePost()
            TwitterAction.OnLogout -> handleClear()
        }
    }

    private fun handleTextChange(text: String) {
        viewModelScope.launch {
            val metrics = computeTweetMetricsUseCase(text)
            _state.update {
                it.copy(
                    text = text,
                    metrics = metrics
                )
            }
            textInput.emit(text)
        }
    }

    private fun handleClear() {
        _state.update {
            it.copy(
                text = "",
                metrics = TweetMetrics(
                    weightedLength = 0,
                    remaining = TwitterConstants.MAX_TWEET_CHARS,
                    withinLimit = true
                ),
                errors = emptyList(),
                isChecking = false
            )
        }
    }

    private fun handleCopy() {
        viewModelScope.launch {
            val text = _state.value.text
            if (text.isEmpty()) {
                _effects.emit(TwitterEffect.ShowToast("Nothing to copy"))
            } else {
                _effects.emit(TwitterEffect.CopyToClipboard(text))
                _effects.emit(TwitterEffect.ShowToast("Copied"))
            }
        }
    }

    private fun handlePost() {
        viewModelScope.launch {
            val text = _state.value.text
            val metrics = _state.value.metrics

            when {
                text.isBlank() -> {
                    _effects.emit(TwitterEffect.ShowToast("Cannot post empty text"))
                    return@launch
                }
                !metrics.withinLimit -> {
                    _state.update { it.copy(postingState = PostingState.Error("Too long ")) }
                    _effects.emit(TwitterEffect.ShowToast("Too long "))
                    return@launch
                }
            }

            _state.update { it.copy(postingState = PostingState.Posting) }

            postTweetUseCase(text).fold(
                onSuccess = {
                    _state.update { it.copy(postingState = PostingState.Success) }
                    _effects.emit(TwitterEffect.ShowToast("Posted!"))
                    delay(POST_SUCCESS_DELAY_MS)
                    handleClear()
                    _state.update { it.copy(postingState = PostingState.Idle) }
                },
                onFailure = { error ->
                    val message = when {
                        error is TweetPublishException &&
                        error.message.equals("No client available", ignoreCase = true) -> "NO CLIENT!"
                        else -> error.message ?: "Post failed"
                    }
                    _state.update { it.copy(postingState = PostingState.Error(message)) }
                    _effects.emit(TwitterEffect.ShowToast(message))
                    delay(POST_ERROR_DELAY_MS)
                    _state.update { it.copy(postingState = PostingState.Idle) }
                }
            )
        }
    }

    private fun observeTextValidation() {
        viewModelScope.launch {
            textInput
                .debounce(TEXT_VALIDATION_DEBOUNCE_MS)
                .collectLatest { text ->
                    if (text.length >= MIN_TEXT_LENGTH_FOR_CHECK) {
                        _state.update { it.copy(isChecking = true) }
                        val issues = checkTextIssuesUseCase(text).getOrElse { emptyList() }
                        _state.update { it.copy(errors = issues, isChecking = false) }
                    } else {
                        _state.update { it.copy(errors = emptyList(), isChecking = false) }
                    }
                }
        }
    }

    fun refreshAuthState() {
        _state.update { it.copy(isAuthenticated = oauthManager.isAuthenticated()) }
    }
}


