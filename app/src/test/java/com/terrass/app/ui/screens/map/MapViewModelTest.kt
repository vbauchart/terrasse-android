package com.terrass.app.ui.screens.map

import android.location.Location
import app.cash.turbine.test
import com.terrass.app.data.location.LocationProvider
import com.terrass.app.domain.model.Terrace
import com.terrass.app.domain.usecase.DeleteTerraceUseCase
import com.terrass.app.domain.usecase.GetTerracesUseCase
import com.terrass.app.domain.usecase.VoteTerraceUseCase
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var locationProvider: LocationProvider
    private lateinit var getTerracesUseCase: GetTerracesUseCase
    private lateinit var voteTerraceUseCase: VoteTerraceUseCase
    private lateinit var deleteTerraceUseCase: DeleteTerraceUseCase
    private lateinit var viewModel: MapViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        locationProvider = mockk(relaxed = true)
        getTerracesUseCase = mockk()
        voteTerraceUseCase = mockk()
        deleteTerraceUseCase = mockk()
        every { getTerracesUseCase() } returns flowOf(emptyList())
        coJustRun { voteTerraceUseCase(any(), any()) }
        coJustRun { deleteTerraceUseCase(any()) }
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = MapViewModel(
        locationProvider, getTerracesUseCase, voteTerraceUseCase, deleteTerraceUseCase,
    )

    @Test
    fun `initial state has Paris as default center`() {
        viewModel = createViewModel()
        val state = viewModel.uiState.value
        assertEquals(48.8566, state.center.latitude, 0.0001)
        assertFalse(state.hasLocationPermission)
    }

    @Test
    fun `terraces are loaded on init`() = runTest {
        val terraces = listOf(
            Terrace(id = 1, name = "Café A", latitude = 43.6, longitude = 1.4),
        )
        every { getTerracesUseCase() } returns flowOf(terraces)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.terraces.size)
    }

    @Test
    fun `permission granted starts tracking`() = runTest {
        val mockLocation = mockk<Location> {
            every { latitude } returns 43.6047
            every { longitude } returns 1.4442
        }
        coEvery { locationProvider.lastLocation() } returns mockLocation
        every { locationProvider.locationUpdates(any()) } returns flowOf(mockLocation)

        viewModel = createViewModel()
        viewModel.onPermissionResult(true)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.hasLocationPermission)
            val updated = awaitItem()
            assertEquals(43.6047, updated.center.latitude, 0.0001)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onMarkerClick selects terrace`() = runTest {
        val terraces = listOf(
            Terrace(id = 1, name = "Café A", latitude = 43.6, longitude = 1.4),
            Terrace(id = 2, name = "Bar B", latitude = 43.7, longitude = 1.5),
        )
        every { getTerracesUseCase() } returns flowOf(terraces)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onMarkerClick(2)
        assertEquals("Bar B", viewModel.uiState.value.selectedTerrace?.name)
    }

    @Test
    fun `onDismissDetail clears selection`() = runTest {
        val terraces = listOf(Terrace(id = 1, name = "Café", latitude = 43.6, longitude = 1.4))
        every { getTerracesUseCase() } returns flowOf(terraces)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onMarkerClick(1)
        assertEquals("Café", viewModel.uiState.value.selectedTerrace?.name)

        viewModel.onDismissDetail()
        assertNull(viewModel.uiState.value.selectedTerrace)
    }

    @Test
    fun `onVote calls use case`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onVote(1, true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { voteTerraceUseCase(1, true) }
    }

    @Test
    fun `onDelete calls use case and clears selection`() = runTest {
        val terraces = listOf(Terrace(id = 1, name = "Café", latitude = 43.6, longitude = 1.4))
        every { getTerracesUseCase() } returns flowOf(terraces)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onMarkerClick(1)
        viewModel.onDelete(1)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { deleteTerraceUseCase(1) }
        assertNull(viewModel.uiState.value.selectedTerrace)
    }
}
