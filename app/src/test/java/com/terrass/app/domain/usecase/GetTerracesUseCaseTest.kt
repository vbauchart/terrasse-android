package com.terrass.app.domain.usecase

import app.cash.turbine.test
import com.terrass.app.domain.model.Environment
import com.terrass.app.domain.model.FilterCriteria
import com.terrass.app.domain.model.NoiseLevel
import com.terrass.app.domain.model.Terrace
import com.terrass.app.domain.repository.TerraceRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetTerracesUseCaseTest {

    private lateinit var repository: TerraceRepository
    private lateinit var useCase: GetTerracesUseCase

    @BeforeEach
    fun setup() {
        repository = mockk()
        useCase = GetTerracesUseCase(repository)
    }

    @Test
    fun `invoke returns flow of terraces from repository`() = runTest {
        val terraces = listOf(
            Terrace(id = 1, name = "Café A", latitude = 43.6, longitude = 1.4),
            Terrace(id = 2, name = "Bar B", latitude = 43.7, longitude = 1.5),
        )
        every { repository.getAllTerraces() } returns flowOf(terraces)

        useCase().test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("Café A", result[0].name)
            assertEquals("Bar B", result[1].name)
            awaitComplete()
        }
    }

    @Test
    fun `invoke returns empty list when no terraces`() = runTest {
        every { repository.getAllTerraces() } returns flowOf(emptyList())

        useCase().test {
            val result = awaitItem()
            assertEquals(0, result.size)
            awaitComplete()
        }
    }

    @Test
    fun `invoke with filter returns only matching terraces`() = runTest {
        val terraces = listOf(
            Terrace(id = 1, name = "Calme", latitude = 43.6, longitude = 1.4,
                environment = Environment(noiseLevel = NoiseLevel.QUIET)),
            Terrace(id = 2, name = "Bruyant", latitude = 43.7, longitude = 1.5,
                environment = Environment(noiseLevel = NoiseLevel.NOISY)),
        )
        every { repository.getAllTerraces() } returns flowOf(terraces)

        val filter = FilterCriteria(noiseLevels = setOf(NoiseLevel.QUIET))
        useCase(filter).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Calme", result[0].name)
            awaitComplete()
        }
    }

    @Test
    fun `invoke with empty filter returns all terraces`() = runTest {
        val terraces = listOf(
            Terrace(id = 1, name = "A", latitude = 43.6, longitude = 1.4),
            Terrace(id = 2, name = "B", latitude = 43.7, longitude = 1.5),
        )
        every { repository.getAllTerraces() } returns flowOf(terraces)

        useCase(FilterCriteria()).test {
            assertEquals(2, awaitItem().size)
            awaitComplete()
        }
    }
}
