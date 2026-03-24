# Terrass — Vue d'ensemble du projet

## Objectif
Application Android permettant de noter et découvrir des terrasses de cafés/restaurants.
Carte OSM interactive, ajout/vote/filtrage de terrasses, import depuis Nominatim (OSM).

## Stack technique
- Kotlin + Jetpack Compose (Material 3)
- Architecture MVVM + Clean Architecture (domain/data/ui)
- Hilt (injection de dépendances)
- Room (base de données locale)
- osmdroid (carte OpenStreetMap)
- Nominatim (recherche d'établissements, sans clé API)
- Coroutines + StateFlow/SharedFlow
- JUnit 5 + MockK (tests unitaires)

## Structure du code
```
app/src/main/java/com/terrass/app/
├── data/
│   ├── local/      # Room DAO, Entity, Mapper
│   ├── location/   # LocationProvider (FusedLocationProviderClient)
│   ├── remote/     # NominatimService (HTTP via HttpURLConnection)
│   └── repository/ # TerraceRepositoryImpl
├── di/             # AppModule, DatabaseModule, RepositoryModule (Hilt)
├── domain/
│   ├── model/      # Terrace, PlaceResult, FilterCriteria, Enums
│   ├── repository/ # TerraceRepository (interface)
│   └── usecase/    # Add/Update/Delete/Get/Vote/SearchPlaces UseCases
├── ui/
│   ├── components/ # Composants réutilisables (map, common)
│   ├── screens/
│   │   ├── addterrace/  # AddEditTerraceScreen + ViewModel
│   │   └── map/         # MapScreen + ViewModel + composants
│   ├── theme/
│   └── TerassApp.kt     # NavGraph
├── MainActivity.kt
└── TerassApplication.kt
```
