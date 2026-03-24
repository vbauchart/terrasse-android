package com.terrass.app.domain.usecase

import com.terrass.app.domain.model.Terrace
import com.terrass.app.domain.repository.TerraceRepository
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdateTerraceUseCaseTest {

    private lateinit var repository: TerraceRepository
    private lateinit var useCase: UpdateTerraceUseCase

    @BeforeEach
    fun setup() {
        repository = mockk()
        useCase = UpdateTerraceUseCase(repository)
    }

    @Test
    fun `invoke with valid terrace returns success`() = runTest {
        val terrace = Terrace(id = 1, name = "Updated", latitude = 43.6, longitude = 1.4)
        coJustRun { repository.updateTerrace(any()) }

        val result = useCase(terrace)

        assertTrue(result.isSuccess)
        coVerify { repository.updateTerrace(terrace) }
    }

    @Test
    fun `invoke with blank name returns failure`() = runTest {
        val terrace = Terrace(id = 1, name = "", latitude = 43.6, longitude = 1.4)

        val result = useCase(terrace)

        assertTrue(result.isFailure)
    }
}
