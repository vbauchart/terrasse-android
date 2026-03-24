# Style et conventions — Terrass

## Kotlin
- Nommage standard Kotlin (camelCase fonctions/variables, PascalCase classes)
- Pas de docstrings/commentaires sauf logique non évidente
- `data class` pour les modèles et états UI
- `sealed interface` pour les événements UI (ex: `AddTerraceEvent`)

## Architecture
- **Clean Architecture** : domain ne dépend pas de data/ui
- **UseCases** : une responsabilité, `operator fun invoke`, retournent `Result<T>`
- **ViewModel** : `StateFlow<UiState>` + `SharedFlow<Event>`, pas de logique UI
- **Hilt** : `@HiltViewModel`, `@Singleton`, `@Inject constructor()` — pas de `@Provides` si `@Inject constructor` suffit
- **Compose** : écrans = fonctions `@Composable`, état hissé dans le ViewModel

## Tests
- JUnit 5 + MockK
- `runTest` pour les coroutines
- `StandardTestDispatcher` + `Dispatchers.setMain/resetMain` dans les tests ViewModel
- Pas de mock de base de données (tests unitaires sur UseCases et ViewModels)
