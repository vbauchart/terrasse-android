package com.terrass.app.domain.usecase

import com.terrass.app.domain.model.Terrace
import com.terrass.app.domain.repository.TerraceRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AddTerraceUseCaseTest {

    private lateinit var repository: TerraceRepository
    private lateinit var useCase: AddTerraceUseCase

    @BeforeEach
    fun setup() {
        repository = mockk()
        useCase = AddTerraceUseCase(repository)
    }

    @Test
    fun `invoke with valid terrace returns success`() = runTest {
        val terrace = Terrace(name = "Café Test", latitude = 43.6, longitude = 1.4)
        coEvery { repository.addTerrace(any()) } returns 1L

        val result = useCase(terrace)

        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
        coVerify { repository.addTerrace(terrace) }
    }

    @Test
    fun `invoke with blank name returns failure`() = runTest {
        val terrace = Terrace(name = "", latitude = 43.6, longitude = 1.4)

        val result = useCase(terrace)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `invoke with whitespace-only name returns failure`() = runTest {
        val terrace = Terrace(name = "   ", latitude = 43.6, longitude = 1.4)

        val result = useCase(terrace)

        assertTrue(result.isFailure)
    }
}
