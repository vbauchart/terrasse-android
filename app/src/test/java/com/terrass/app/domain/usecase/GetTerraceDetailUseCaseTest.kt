package com.terrass.app.domain.usecase

import app.cash.turbine.test
import com.terrass.app.domain.model.Terrace
import com.terrass.app.domain.repository.TerraceRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetTerraceDetailUseCaseTest {

    private lateinit var repository: TerraceRepository
    private lateinit var useCase: GetTerraceDetailUseCase

    @BeforeEach
    fun setup() {
        repository = mockk()
        useCase = GetTerraceDetailUseCase(repository)
    }

    @Test
    fun `invoke returns terrace with votes`() = runTest {
        val terrace = Terrace(
            id = 1, name = "Café", latitude = 43.6, longitude = 1.4,
            thumbsUp = 8, thumbsDown = 2,
        )
        every { repository.getTerraceById(1) } returns flowOf(terrace)

        useCase(1).test {
            val result = awaitItem()!!
            assertEquals("Café", result.name)
            assertEquals(8, result.thumbsUp)
            assertEquals(2, result.thumbsDown)
            assertEquals(80, result.votePercentage)
            awaitComplete()
        }
    }

    @Test
    fun `invoke returns null for unknown id`() = runTest {
        every { repository.getTerraceById(999) } returns flowOf(null)

        useCase(999).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }
}
