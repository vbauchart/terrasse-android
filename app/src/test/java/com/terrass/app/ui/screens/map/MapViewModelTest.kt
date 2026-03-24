package com.terrass.app.ui.screens.map

import android.location.Location
import app.cash.turbine.test
import com.terrass.app.data.location.LocationProvider
import com.terrass.app.domain.model.Environment
import com.terrass.app.domain.model.FilterCriteria
import com.terrass.app.domain.model.NoiseLevel
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
import org.junit.jupiter.api.Assertions.assertNotNull
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
        every { getTerracesUseCase(any()) } returns flowOf(emptyList())
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
        every { getTerracesUseCase(any()) } returns flowOf(terraces)

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
        every { getTerracesUseCase(any()) } returns flowOf(terraces)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onMarkerClick(2)
        assertEquals("Bar B", viewModel.uiState.value.selectedTerrace?.name)
    }

    @Test
    fun `onDismissDetail clears selection`() = runTest {
        val terraces = listOf(Terrace(id = 1, name = "Café", latitude = 43.6, longitude = 1.4))
        every { getTerracesUseCase(any()) } returns flowOf(terraces)

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
        every { getTerracesUseCase(any()) } returns flowOf(terraces)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onMarkerClick(1)
        viewModel.onDelete(1)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { deleteTerraceUseCase(1) }
        assertNull(viewModel.uiState.value.selectedTerrace)
    }

    @Test
    fun `onCenterOnUser without permission sets error`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onCenterOnUser()

        assertNotNull(viewModel.uiState.value.locationError)
        assertFalse(viewModel.uiState.value.isLocating)
    }

    @Test
    fun `onCenterOnUser with permission and location zooms in`() = runTest {
        val mockLocation = mockk<Location> {
            every { latitude } returns 43.6047
            every { longitude } returns 1.4442
        }
        coEvery { locationProvider.lastLocation() } returns mockLocation

        viewModel = createViewModel()
        viewModel.onPermissionResult(true)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onCenterOnUser()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(43.6047, state.center.latitude, 0.0001)
        assertEquals(15.0, state.zoom, 0.1)
        assertFalse(state.isLocating)
        assertNull(state.locationError)
    }

    @Test
    fun `onCenterOnUser with permission but no location sets error`() = runTest {
        coEvery { locationProvider.lastLocation() } returns null

        viewModel = createViewModel()
        viewModel.onPermissionResult(true)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onCenterOnUser()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.locationError)
        assertFalse(state.isLocating)
    }

    @Test
    fun `onDismissLocationError clears error`() = runTest {
        viewModel = createViewModel()
        viewModel.onCenterOnUser() // sans permission → erreur
        assertNotNull(viewModel.uiState.value.locationError)

        viewModel.onDismissLocationError()
        assertNull(viewModel.uiState.value.locationError)
    }

    @Test
    fun `onFilterChange updates filter and reloads terraces`() = runTest {
        val allTerraces = listOf(
            Terrace(id = 1, name = "Calme", latitude = 43.6, longitude = 1.4,
                environment = Environment(noiseLevel = NoiseLevel.QUIET)),
            Terrace(id = 2, name = "Bruyant", latitude = 43.7, longitude = 1.5,
                environment = Environment(noiseLevel = NoiseLevel.NOISY)),
        )
        every { getTerracesUseCase(FilterCriteria()) } returns flowOf(allTerraces)
        val quietFilter = FilterCriteria(noiseLevels = setOf(NoiseLevel.QUIET))
        every { getTerracesUseCase(quietFilter) } returns flowOf(listOf(allTerraces[0]))

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.terraces.size)

        viewModel.onFilterChange(quietFilter)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.terraces.size)
        assertEquals("Calme", viewModel.uiState.value.terraces[0].name)
        assertEquals(quietFilter, viewModel.uiState.value.filter)
    }

    @Test
    fun `onResetFilter clears all filters`() = runTest {
        every { getTerracesUseCase(any()) } returns flowOf(emptyList())

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onFilterChange(FilterCriteria(noiseLevels = setOf(NoiseLevel.QUIET)))
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.filter.activeCount > 0)

        viewModel.onResetFilter()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.filter.activeCount)
    }

    @Test
    fun `onTerraceSelected sets terrace and zooms`() = runTest {
        val terrace = Terrace(id = 1, name = "Café", latitude = 43.6, longitude = 1.4)
        every { getTerracesUseCase(any()) } returns flowOf(listOf(terrace))

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onTerraceSelected(terrace)
        val state = viewModel.uiState.value
        assertEquals("Café", state.selectedTerrace?.name)
        assertEquals(43.6, state.center.latitude, 0.0001)
        assertEquals(17.0, state.zoom, 0.1)
    }

    @Test
    fun `onToggleViewMode switches between map and list`() = runTest {
        viewModel = createViewModel()
        assertEquals(ViewMode.MAP, viewModel.uiState.value.viewMode)

        viewModel.onToggleViewMode()
        assertEquals(ViewMode.LIST, viewModel.uiState.value.viewMode)

        viewModel.onToggleViewMode()
        assertEquals(ViewMode.MAP, viewModel.uiState.value.viewMode)
    }

    @Test
    fun `onToggleFilterSheet shows and hides filter sheet`() = runTest {
        viewModel = createViewModel()
        assertFalse(viewModel.uiState.value.isFilterSheetVisible)

        viewModel.onToggleFilterSheet()
        assertTrue(viewModel.uiState.value.isFilterSheetVisible)

        viewModel.onDismissFilterSheet()
        assertFalse(viewModel.uiState.value.isFilterSheetVisible)
    }
}
