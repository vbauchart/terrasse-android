package com.terrass.app.domain.usecase

import com.terrass.app.domain.repository.TerraceRepository
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DeleteTerraceUseCaseTest {

    private lateinit var repository: TerraceRepository
    private lateinit var useCase: DeleteTerraceUseCase

    @BeforeEach
    fun setup() {
        repository = mockk()
        useCase = DeleteTerraceUseCase(repository)
    }

    @Test
    fun `invoke calls repository deleteTerrace with correct id`() = runTest {
        coJustRun { repository.deleteTerrace(any()) }

        useCase(99)

        coVerify { repository.deleteTerrace(99) }
    }
}
