package com.terrass.app.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TerraceTest {

    @Test
    fun `votePercentage returns -1 when no votes`() {
        val terrace = Terrace(name = "Test", latitude = 0.0, longitude = 0.0)
        assertEquals(-1, terrace.votePercentage)
    }

    @Test
    fun `votePercentage returns 100 when all positive`() {
        val terrace = Terrace(name = "Test", latitude = 0.0, longitude = 0.0, thumbsUp = 5, thumbsDown = 0)
        assertEquals(100, terrace.votePercentage)
    }

    @Test
    fun `votePercentage returns 0 when all negative`() {
        val terrace = Terrace(name = "Test", latitude = 0.0, longitude = 0.0, thumbsUp = 0, thumbsDown = 3)
        assertEquals(0, terrace.votePercentage)
    }

    @Test
    fun `votePercentage calculates correctly for mixed votes`() {
        val terrace = Terrace(name = "Test", latitude = 0.0, longitude = 0.0, thumbsUp = 8, thumbsDown = 2)
        assertEquals(80, terrace.votePercentage)
    }

    @Test
    fun `totalVotes returns sum of thumbsUp and thumbsDown`() {
        val terrace = Terrace(name = "Test", latitude = 0.0, longitude = 0.0, thumbsUp = 3, thumbsDown = 7)
        assertEquals(10, terrace.totalVotes)
    }
}
