package com.moe.twitter.presentation.twitter

import app.cash.turbine.test
import com.moe.twitter.data.remote.auth.OAuthManager
import com.moe.twitter.domain.TwitterConstants
import com.moe.twitter.domain.model.TextIssue
import com.moe.twitter.domain.model.TweetMetrics
import com.moe.twitter.domain.model.TweetPublishException
import com.moe.twitter.domain.usecase.CheckTextIssuesUseCase
import com.moe.twitter.domain.usecase.ComputeTweetMetricsUseCase
import com.moe.twitter.domain.usecase.PostTweetUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TwitterViewModelTest {

    private lateinit var viewModel: TwitterViewModel
    private lateinit var postTweetUseCase: PostTweetUseCase
    private lateinit var checkTextIssuesUseCase: CheckTextIssuesUseCase
    private lateinit var computeTweetMetricsUseCase: ComputeTweetMetricsUseCase
    private lateinit var oauthManager: OAuthManager

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        postTweetUseCase = mockk()
        checkTextIssuesUseCase = mockk()
        computeTweetMetricsUseCase = mockk()
        oauthManager = mockk()

        // Default mock behaviors
        every { oauthManager.isAuthenticated() } returns true
        every { oauthManager.startAuthFlow() } returns "https://mock.auth.url"
        // Default mock for text validation to avoid "no answer" errors
        coEvery { checkTextIssuesUseCase(any(), any()) } returns Result.success(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): TwitterViewModel {
        return TwitterViewModel(
            postTweetUseCase = postTweetUseCase,
            checkTextIssuesUseCase = checkTextIssuesUseCase,
            computeTweetMetricsUseCase = computeTweetMetricsUseCase,
            oauthManager = oauthManager
        )
    }

    @Test
    fun `initial state should have empty text and full remaining characters`() = runTest {
        viewModel = createViewModel()

        val state = viewModel.state.value
        assertEquals("", state.text)
        assertEquals(0, state.metrics.weightedLength)
        assertEquals(TwitterConstants.MAX_TWEET_CHARS, state.metrics.remaining)
        assertTrue(state.metrics.withinLimit)
        assertTrue(state.errors.isEmpty())
        assertFalse(state.isChecking)
        assertEquals(PostingState.Idle, state.postingState)
        assertTrue(state.isAuthenticated)
    }

    @Test
    fun `initial state should start auth flow when not authenticated`() = runTest {
        every { oauthManager.isAuthenticated() } returns false

        viewModel = createViewModel()

        verify { oauthManager.startAuthFlow() }
        assertFalse(viewModel.state.value.isAuthenticated)
    }

    @Test
    fun `onTextChange should update text and metrics`() = runTest {
        val testText = "Hello Twitter!"
        val testMetrics = TweetMetrics(
            weightedLength = 14,
            remaining = 266,
            withinLimit = true
        )

        coEvery { computeTweetMetricsUseCase(testText) } returns testMetrics

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(TwitterAction.OnTextChange(testText))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(testText, state.text)
        assertEquals(testMetrics, state.metrics)
        coVerify { computeTweetMetricsUseCase(testText) }
    }

    @Test
    fun `onTextChange with long text should trigger validation after debounce`() = runTest {
        val testText = "This is a longer text for validation"
        val testMetrics = TweetMetrics(38, 242, true)
        val testIssues = listOf(
            TextIssue(
                start = 10,
                end = 16,
                message = "Grammar error",
                issueType = "grammar"
            )
        )

        coEvery { computeTweetMetricsUseCase(testText) } returns testMetrics
        coEvery { checkTextIssuesUseCase(testText, any()) } returns Result.success(testIssues)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(TwitterAction.OnTextChange(testText))
        // Don't advance yet - we want to check state before debounce fires

        // Should not check immediately
        assertFalse(viewModel.state.value.isChecking)
        assertTrue(viewModel.state.value.errors.isEmpty())

        // Advance past debounce time (600ms)
        advanceTimeBy(600)
        advanceUntilIdle()

        // Now should have checked and updated errors
        assertEquals(testIssues, viewModel.state.value.errors)
        assertFalse(viewModel.state.value.isChecking)
        coVerify { checkTextIssuesUseCase(testText, any()) }
    }

    @Test
    fun `onTextChange with short text should not trigger validation`() = runTest {
        val testText = "Hi"
        val testMetrics = TweetMetrics(2, 278, true)

        coEvery { computeTweetMetricsUseCase(testText) } returns testMetrics

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(TwitterAction.OnTextChange(testText))
        advanceUntilIdle()

        // Advance past debounce time
        advanceTimeBy(600)
        advanceUntilIdle()

        // Should not check text less than 5 characters
        assertTrue(viewModel.state.value.errors.isEmpty())
        assertFalse(viewModel.state.value.isChecking)
        coVerify(exactly = 0) { checkTextIssuesUseCase(any()) }
    }

    @Test
    fun `onTextChange should handle validation errors gracefully`() = runTest {
        val testText = "Test text with network error"
        val testMetrics = TweetMetrics(29, 251, true)

        coEvery { computeTweetMetricsUseCase(testText) } returns testMetrics
        coEvery { checkTextIssuesUseCase(testText) } returns Result.failure(Exception("Network error"))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(TwitterAction.OnTextChange(testText))
        advanceUntilIdle()

        advanceTimeBy(600)
        advanceUntilIdle()

        // Should have empty errors on failure
        assertTrue(viewModel.state.value.errors.isEmpty())
        assertFalse(viewModel.state.value.isChecking)
    }

    @Test
    fun `onClear should reset all state`() = runTest {
        val testText = "Some text"
        val testMetrics = TweetMetrics(9, 271, true)

        coEvery { computeTweetMetricsUseCase(testText) } returns testMetrics

        viewModel = createViewModel()
        advanceUntilIdle()

        // Set some text first
        viewModel.onAction(TwitterAction.OnTextChange(testText))
        advanceUntilIdle()

        // Now clear
        viewModel.onAction(TwitterAction.OnClear)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("", state.text)
        assertEquals(0, state.metrics.weightedLength)
        assertEquals(TwitterConstants.MAX_TWEET_CHARS, state.metrics.remaining)
        assertTrue(state.metrics.withinLimit)
        assertTrue(state.errors.isEmpty())
        assertFalse(state.isChecking)
    }

    @Test
    fun `onCopy with empty text should emit nothing to copy effect`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onAction(TwitterAction.OnCopy)
            advanceUntilIdle()

            assertEquals(TwitterEffect.ShowToast("Nothing to copy"), awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `onCopy with text should emit copy and toast effects`() = runTest {
        val testText = "Copy this text"
        val testMetrics = TweetMetrics(14, 266, true)

        coEvery { computeTweetMetricsUseCase(testText) } returns testMetrics

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(TwitterAction.OnTextChange(testText))
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onAction(TwitterAction.OnCopy)
            advanceUntilIdle()

            assertEquals(TwitterEffect.CopyToClipboard(testText), awaitItem())
            assertEquals(TwitterEffect.ShowToast("Copied"), awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `onPost with empty text should emit error toast`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onAction(TwitterAction.OnPost)
            advanceUntilIdle()

            assertEquals(TwitterEffect.ShowToast("Cannot post empty text"), awaitItem())
            expectNoEvents()
        }

        assertEquals(PostingState.Idle, viewModel.state.value.postingState)
    }

    @Test
    fun `onPost with text exceeding limit should emit error toast`() = runTest {
        val longText = "a".repeat(300)
        val testMetrics = TweetMetrics(300, -20, false)

        coEvery { computeTweetMetricsUseCase(longText) } returns testMetrics

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(TwitterAction.OnTextChange(longText))
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onAction(TwitterAction.OnPost)
            advanceUntilIdle()

            assertEquals(TwitterEffect.ShowToast("Text exceeds limit"), awaitItem())
            expectNoEvents()
        }

        assertEquals(PostingState.Idle, viewModel.state.value.postingState)
    }

    @Test
    fun `onPost with valid text should post successfully and clear after delay`() = runTest {
        val testText = "Valid tweet"
        val testMetrics = TweetMetrics(11, 269, true)

        coEvery { computeTweetMetricsUseCase(testText) } returns testMetrics
        coEvery { postTweetUseCase(testText) } returns Result.success(Unit)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(TwitterAction.OnTextChange(testText))
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onAction(TwitterAction.OnPost)

            // Should emit success toast
            assertEquals(TwitterEffect.ShowToast("Posted!"), awaitItem())

            expectNoEvents()
        }

        // Advance past success delay (1800ms)
        advanceTimeBy(1800)
        advanceUntilIdle()

        // Should be cleared and back to idle
        assertEquals("", viewModel.state.value.text)
        assertEquals(PostingState.Idle, viewModel.state.value.postingState)

        coVerify { postTweetUseCase(testText) }
    }

    @Test
    fun `onPost failure should show error and return to idle after delay`() = runTest {
        val testText = "Valid tweet"
        val testMetrics = TweetMetrics(11, 269, true)
        val errorMessage = "Network error"

        coEvery { computeTweetMetricsUseCase(testText) } returns testMetrics
        coEvery { postTweetUseCase(testText) } returns Result.failure(Exception(errorMessage))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(TwitterAction.OnTextChange(testText))
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onAction(TwitterAction.OnPost)

            // Should emit error toast
            assertEquals(TwitterEffect.ShowToast(errorMessage), awaitItem())

            expectNoEvents()
        }

        // Check error state
        val postingState = viewModel.state.value.postingState
        assertTrue(postingState is PostingState.Error)
        assertEquals(errorMessage, (postingState as PostingState.Error).message)

        // Advance past error delay (2000ms)
        advanceTimeBy(2000)
        advanceUntilIdle()

        // Should be back to idle (text NOT cleared on error)
        assertEquals(testText, viewModel.state.value.text)
        assertEquals(PostingState.Idle, viewModel.state.value.postingState)

        coVerify { postTweetUseCase(testText) }
    }

    @Test
    fun `onPost with TweetPublishException no client should show specific error message`() = runTest {
        val testText = "Valid tweet"
        val testMetrics = TweetMetrics(11, 269, true)

        coEvery { computeTweetMetricsUseCase(testText) } returns testMetrics
        coEvery { postTweetUseCase(testText) } returns Result.failure(
            TweetPublishException("No client available")
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(TwitterAction.OnTextChange(testText))
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onAction(TwitterAction.OnPost)

            // Should emit specific error message
            assertEquals(TwitterEffect.ShowToast("NO CLIENT!"), awaitItem())

            expectNoEvents()
        }

        // Check error state with specific message
        val postingState = viewModel.state.value.postingState
        assertTrue(postingState is PostingState.Error)
        assertEquals("NO CLIENT!", (postingState as PostingState.Error).message)

        advanceTimeBy(2000)
        advanceUntilIdle()

        assertEquals(PostingState.Idle, viewModel.state.value.postingState)
    }

    @Test
    fun `refreshAuthState should update authentication status`() = runTest {
        every { oauthManager.isAuthenticated() } returns true

        viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isAuthenticated)

        // Change auth state
        every { oauthManager.isAuthenticated() } returns false

        viewModel.refreshAuthState()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isAuthenticated)

        verify(atLeast = 2) { oauthManager.isAuthenticated() }
    }

    @Test
    fun `multiple rapid text changes should only trigger one validation after debounce`() = runTest {
        val metrics1 = TweetMetrics(5, 275, true)
        val metrics2 = TweetMetrics(10, 270, true)
        val metrics3 = TweetMetrics(15, 265, true)

        coEvery { computeTweetMetricsUseCase("Text1") } returns metrics1
        coEvery { computeTweetMetricsUseCase("Text12") } returns metrics2
        coEvery { computeTweetMetricsUseCase("Text123") } returns metrics3
        coEvery { checkTextIssuesUseCase("Text123", any()) } returns Result.success(emptyList())

        viewModel = createViewModel()
        advanceUntilIdle()

        // Rapid text changes
        viewModel.onAction(TwitterAction.OnTextChange("Text1"))
        advanceTimeBy(100)
        viewModel.onAction(TwitterAction.OnTextChange("Text12"))
        advanceTimeBy(100)
        viewModel.onAction(TwitterAction.OnTextChange("Text123"))

        advanceUntilIdle()

        // Advance past debounce
        advanceTimeBy(600)
        advanceUntilIdle()

        // Should only check the last text once
        coVerify(exactly = 1) { checkTextIssuesUseCase("Text123", any()) }
        coVerify(exactly = 0) { checkTextIssuesUseCase("Text1", any()) }
        coVerify(exactly = 0) { checkTextIssuesUseCase("Text12", any()) }
    }

    @Test
    fun `onLogout should clear state`() = runTest {
        val testText = "Some text"
        val testMetrics = TweetMetrics(9, 271, true)

        coEvery { computeTweetMetricsUseCase(testText) } returns testMetrics

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(TwitterAction.OnTextChange(testText))
        advanceUntilIdle()

        viewModel.onAction(TwitterAction.OnLogout)
        advanceUntilIdle()

        // Should clear like OnClear
        assertEquals("", viewModel.state.value.text)
        assertEquals(0, viewModel.state.value.metrics.weightedLength)
    }
}
