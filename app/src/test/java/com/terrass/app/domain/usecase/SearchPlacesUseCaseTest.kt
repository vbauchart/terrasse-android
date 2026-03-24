package com.terrass.app.domain.usecase

import com.terrass.app.data.remote.PhotonService
import com.terrass.app.domain.model.PlaceResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SearchPlacesUseCaseTest {

    private lateinit var photonService: PhotonService
    private lateinit var useCase: SearchPlacesUseCase

    @BeforeEach
    fun setup() {
        photonService = mockk()
        useCase = SearchPlacesUseCase(photonService)
    }

    @Test
    fun `invoke returns success with results from service`() = runTest {
        val expected = listOf(
            PlaceResult(
                name = "Café de Flore",
                displayName = "Café de Flore, 172 Bd Saint-Germain, Paris",
                latitude = 48.854,
                longitude = 2.332,
                address = "172 Bd Saint-Germain, Paris",
            )
        )
        coEvery { photonService.search("Café de Flore", any()) } returns expected

        val result = useCase("Café de Flore")

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
        coVerify { photonService.search("Café de Flore", null) }
    }

    @Test
    fun `invoke propagates service exception as failure`() = runTest {
        coEvery { photonService.search(any(), any()) } throws RuntimeException("Network error")

        val result = useCase("query")

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke returns empty list when service returns no results`() = runTest {
        coEvery { photonService.search(any(), any()) } returns emptyList()

        val result = useCase("xyz unknown place")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }
}
