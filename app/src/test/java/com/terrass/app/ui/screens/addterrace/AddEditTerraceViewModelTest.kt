package com.terrass.app.ui.screens.addterrace

import androidx.lifecycle.SavedStateHandle
import com.terrass.app.domain.model.PlaceResult
import com.terrass.app.domain.usecase.AddTerraceUseCase
import com.terrass.app.domain.usecase.GetTerraceDetailUseCase
import com.terrass.app.domain.usecase.SearchPlacesUseCase
import com.terrass.app.domain.usecase.UpdateTerraceUseCase
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddEditTerraceViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: AddEditTerraceViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AddEditTerraceViewModel(
            savedStateHandle = SavedStateHandle(),
            addTerraceUseCase = mockk(),
            updateTerraceUseCase = mockk(),
            getTerraceDetailUseCase = mockk(),
            searchPlacesUseCase = mockk(),
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `applyPlaceResult pre-fills name, lat, lng and address`() = runTest {
        val place = PlaceResult(
            name = "Café de Flore",
            displayName = "Café de Flore, 172 Bd Saint-Germain, Paris",
            latitude = 48.854,
            longitude = 2.332,
            address = "172 Bd Saint-Germain, Paris",
        )

        viewModel.applyPlaceResult(place)

        val state = viewModel.uiState.value
        assertEquals("Café de Flore", state.name)
        assertEquals(48.854, state.latitude)
        assertEquals(2.332, state.longitude)
        assertEquals("172 Bd Saint-Germain, Paris", state.address)
        assertNull(state.nameError)
    }

    @Test
    fun `applyPlaceResult clears search state`() = runTest {
        val place = PlaceResult(
            name = "Café Test",
            displayName = "Café Test, Paris",
            latitude = 48.8,
            longitude = 2.3,
            address = null,
        )

        viewModel.applyPlaceResult(place)

        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertEquals(emptyList<PlaceResult>(), state.searchResults)
    }

    @Test
    fun `applyPlaceResult with null address sets address to null`() = runTest {
        val place = PlaceResult(
            name = "Bar Test",
            displayName = "Bar Test",
            latitude = 45.0,
            longitude = 3.0,
            address = null,
        )

        viewModel.applyPlaceResult(place)

        assertNull(viewModel.uiState.value.address)
    }
}
