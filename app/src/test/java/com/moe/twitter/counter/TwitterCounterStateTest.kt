package com.moe.twitter.counter

import com.twitter.twittertext.TwitterTextParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TwitterCounterStateTest {

    @Test
    fun emojiCountsWithWeight() {
        val result = TwitterTextParser().parseTweet("🤝")

        // Twitter weighted length treats many emoji as weight 2
        assertEquals(2, result.weightedLength)
        assertTrue(result.isValid)
    }

    @Test
    fun mixedTextWithinLimit() {
        val text = "Compose + 🤝 + TwitterTextParser"
        val result = TwitterTextParser().parseTweet(text)

        assertTrue(result.isValid)
        assertTrue(result.weightedLength <= 280)
    }
}

