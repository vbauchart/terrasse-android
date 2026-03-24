package com.terrass.app.domain.usecase

import com.terrass.app.domain.repository.TerraceRepository
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VoteTerraceUseCaseTest {

    private lateinit var repository: TerraceRepository
    private lateinit var useCase: VoteTerraceUseCase

    @BeforeEach
    fun setup() {
        repository = mockk()
        useCase = VoteTerraceUseCase(repository)
    }

    @Test
    fun `invoke calls repository vote with correct params for thumbs up`() = runTest {
        coJustRun { repository.vote(any(), any()) }

        useCase(terraceId = 42, isPositive = true)

        coVerify { repository.vote(42, true) }
    }

    @Test
    fun `invoke calls repository vote with correct params for thumbs down`() = runTest {
        coJustRun { repository.vote(any(), any()) }

        useCase(terraceId = 7, isPositive = false)

        coVerify { repository.vote(7, false) }
    }
}
