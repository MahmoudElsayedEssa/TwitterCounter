package com.moe.twitter.domain.usecase

import com.moe.twitter.domain.model.TextIssue
import com.moe.twitter.domain.repository.TextCheckRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CheckTextIssuesUseCaseTest {

    private lateinit var textCheckRepository: TextCheckRepository
    private lateinit var useCase: CheckTextIssuesUseCase

    @Before
    fun setup() {
        textCheckRepository = mockk()
        useCase = CheckTextIssuesUseCase(repository = textCheckRepository)
    }

    @Test
    fun `invoke should return text issues from repository`() = runTest {
        val testText = "This are a test"
        val expectedIssues = listOf(
            TextIssue(
                start = 5,
                end = 8,
                message = "Grammar error",
                issueType = "grammar",
                ruleId = "SUBJECT_VERB_AGREEMENT"
            )
        )

        coEvery {
            textCheckRepository.checkTextIssues(testText, "en-US")
        } returns Result.success(expectedIssues)

        val result = useCase(testText)

        assertTrue(result.isSuccess)
        assertEquals(expectedIssues, result.getOrNull())
        coVerify { textCheckRepository.checkTextIssues(testText, "en-US") }
    }

    @Test
    fun `invoke with custom language should pass language parameter`() = runTest {
        val testText = "Bonjour le monde"
        val language = "fr-FR"
        val expectedIssues = emptyList<TextIssue>()

        coEvery {
            textCheckRepository.checkTextIssues(testText, language)
        } returns Result.success(expectedIssues)

        val result = useCase(testText, language)

        assertTrue(result.isSuccess)
        assertEquals(expectedIssues, result.getOrNull())
        coVerify { textCheckRepository.checkTextIssues(testText, language) }
    }

    @Test
    fun `invoke should use default language when not specified`() = runTest {
        val testText = "Hello world"
        val expectedIssues = emptyList<TextIssue>()

        coEvery {
            textCheckRepository.checkTextIssues(testText, "en-US")
        } returns Result.success(expectedIssues)

        val result = useCase(testText)

        assertTrue(result.isSuccess)
        coVerify { textCheckRepository.checkTextIssues(testText, "en-US") }
    }

    @Test
    fun `invoke should return empty list when no issues found`() = runTest {
        val testText = "This is a correct sentence."
        val expectedIssues = emptyList<TextIssue>()

        coEvery {
            textCheckRepository.checkTextIssues(testText, "en-US")
        } returns Result.success(expectedIssues)

        val result = useCase(testText)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isEmpty() ?: false)
        coVerify { textCheckRepository.checkTextIssues(testText, "en-US") }
    }

    @Test
    fun `invoke should return failure when repository fails`() = runTest {
        val testText = "Test text"
        val errorMessage = "Network error"

        coEvery {
            textCheckRepository.checkTextIssues(testText, "en-US")
        } returns Result.failure(Exception(errorMessage))

        val result = useCase(testText)

        assertTrue(result.isFailure)
        assertEquals(errorMessage, result.exceptionOrNull()?.message)
        coVerify { textCheckRepository.checkTextIssues(testText, "en-US") }
    }

    @Test
    fun `invoke with multiple issues should return all issues`() = runTest {
        val testText = "This are a bad test sentense"
        val expectedIssues = listOf(
            TextIssue(
                start = 5,
                end = 8,
                message = "Grammar error",
                issueType = "grammar"
            ),
            TextIssue(
                start = 20,
                end = 28,
                message = "Spelling error",
                issueType = "typographical"
            )
        )

        coEvery {
            textCheckRepository.checkTextIssues(testText, "en-US")
        } returns Result.success(expectedIssues)

        val result = useCase(testText)

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertEquals(expectedIssues, result.getOrNull())
    }

    @Test
    fun `invoke with empty text should call repository`() = runTest {
        val emptyText = ""
        val expectedIssues = emptyList<TextIssue>()

        coEvery {
            textCheckRepository.checkTextIssues(emptyText, "en-US")
        } returns Result.success(expectedIssues)

        val result = useCase(emptyText)

        assertTrue(result.isSuccess)
        coVerify { textCheckRepository.checkTextIssues(emptyText, "en-US") }
    }
}
