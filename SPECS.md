# Terrasse — Spécifications Fonctionnelles & Architecture Technique

## Contexte

Application Android de recensement et d'évaluation des terrasses de cafés et restaurants.
L'objectif est de permettre aux utilisateurs de découvrir, ajouter et noter des terrasses
selon des critères précis (soleil, bruit, confort...) via une carte interactive OpenStreetMap.

**MVP** : stockage local uniquement (Room), pas d'authentification, pas de backend.

---

## 1. Spécifications Fonctionnelles

### 1.1 Écran principal — Carte interactive
- Carte osmdroid (tuiles OpenStreetMap) en plein écran
- Marqueurs colorés sur chaque terrasse (vert = bien noté, rouge = mal noté, gris = pas de vote)
- Tap sur un marqueur → bottom sheet de détail
- Long-press sur la carte → proposer d'ajouter une terrasse à cet endroit
- Bouton de recentrage sur la position GPS de l'utilisateur
- FAB "+" pour ajouter une terrasse (GPS auto-détecté)
- FAB/icône filtre avec badge du nombre de filtres actifs

### 1.2 Liste des terrasses (bottom sheet)
- Bottom sheet intégré à l'écran carte (swipe-up depuis un peek)
- Peek : barre de drag + résumé ("12 terrasses")
- Liste `LazyColumn` : nom, % positif, 2-3 chips d'attributs principaux
- Tap sur un item → affiche le détail dans le sheet + zoom sur le marqueur

### 1.3 Détail d'une terrasse (bottom sheet)
- Nom, adresse (si dispo)
- Tous les attributs organisés en 4 sections
- Votes : pourcentage positif + nombre total ("82% positif · 17 avis")
- Boutons pouce haut / pouce bas pour voter
- Boutons modifier / supprimer

### 1.4 Ajout / Modification d'une terrasse (plein écran)
- Mini-carte avec pin déplaçable pour choisir l'emplacement
- Pré-rempli avec la position GPS ou les coordonnées du long-press
- Champ nom (obligatoire)
- 4 sections dépliables d'attributs :

**Exposition au soleil**
| Attribut | Type | Valeurs |
|----------|------|---------|
| Orientation | Choix unique | N, S, E, O, NE, NO, SE, SO |
| Exposition | Choix unique | Plein soleil, Partiel, Ombre |

**Confort & équipement**
| Attribut | Type | Valeurs |
|----------|------|---------|
| Couverte | Booléen | Oui / Non |
| Chauffée | Booléen | Oui / Non |
| Mobilier | Choix unique | Chaises, Bancs, Lounge, Mixte |
| Taille | Choix unique | Petite, Moyenne, Grande |

**Environnement**
| Attribut | Type | Valeurs |
|----------|------|---------|
| Proximité route | Choix unique | Aucune, Faible, Moyenne, Forte |
| Bruit | Choix unique | Calme, Modéré, Bruyant |
| Vue | Choix unique | Aucune, Partielle, Belle, Exceptionnelle |
| Végétation | Booléen | Oui / Non |

**Service & prix**
| Attribut | Type | Valeurs |
|----------|------|---------|
| Qualité service | Choix unique | Médiocre, Moyen, Bon, Excellent |
| Gamme de prix | Choix unique | Bon marché, Moyen, Cher |
| Type de cuisine | Texte libre | — |

### 1.5 Filtres
- Bottom sheet ou dialog avec des chip groups par catégorie
- Chaque attribut est un `FilterChip` Material3
- Filtre supplémentaire : "Minimum X% positif"
- Bouton "Réinitialiser"
- Les filtres s'appliquent à la fois à la liste et aux marqueurs de la carte

### 1.6 Système de votes
- Pouce haut / pouce bas (binaire)
- Affiché en pourcentage positif + nombre total
- Pas de limite de votes par terrasse dans le MVP (pas d'auth)
- -1 ou "Pas encore noté" si aucun vote

### 1.7 Modération (post-MVP)
- Champ `status` déjà présent en base ("active", "pending", "hidden")
- Le DAO filtre déjà sur `status = 'active'`
- Prêt pour un futur système de validation sans migration de schéma

---

## 2. Architecture Technique

### 2.1 Stack
- **Kotlin 2.1** + **Jetpack Compose** (Material3)
- **Hilt** (injection de dépendances)
- **Room** (base de données locale)
- **Navigation Compose** (routing)
- **osmdroid 6.1.20** (cartes OpenStreetMap, wrappé dans `AndroidView`)
- **play-services-location** (géolocalisation GPS)
- **Coroutines + Flow** (asynchrone et réactivité)

### 2.2 Architecture MVVM / Clean Architecture

```
UI (Compose) → ViewModel → UseCase → Repository (interface) → DAO (Room)
```

### 2.3 Schéma de base de données

**Table `terraces`**
```
id              INTEGER PRIMARY KEY AUTOINCREMENT
name            TEXT NOT NULL
latitude        REAL NOT NULL
longitude       REAL NOT NULL
address         TEXT
orientation     TEXT        -- "north","south","east","west","northeast",...
sun_exposure    TEXT        -- "full_sun","partial","shade"
is_covered      INTEGER     -- 0/1
is_heated       INTEGER     -- 0/1
furniture_type  TEXT        -- "chairs","benches","lounge","mixed"
size            TEXT        -- "small","medium","large"
road_proximity  TEXT        -- "none","low","medium","high"
noise_level     TEXT        -- "quiet","moderate","noisy"
view_quality    TEXT        -- "none","partial","good","exceptional"
has_vegetation  INTEGER     -- 0/1
service_quality TEXT        -- "poor","average","good","excellent"
price_range     TEXT        -- "cheap","moderate","expensive"
cuisine_type    TEXT
created_at      INTEGER
updated_at      INTEGER
status          TEXT DEFAULT 'active'
```

**Table `votes`**
```
id              INTEGER PRIMARY KEY AUTOINCREMENT
terrace_id      INTEGER REFERENCES terraces(id) ON DELETE CASCADE
is_positive     INTEGER     -- 0/1
created_at      INTEGER
```

### 2.4 Couche Domain — Modèles

- `Terrace` : modèle principal avec sous-objets `SunExposure`, `Comfort`, `Environment`, `Service`
- `FilterCriteria` : data class contenant tous les critères de filtre
- Enums Kotlin : `Orientation`, `ExposureType`, `FurnitureType`, `TerraceSize`, `NoiseLevel`, `ViewQuality`, `ServiceQuality`, `PriceRange`, `TerraceStatus`
- `TerraceRepository` : interface dans le domain

### 2.5 Use Cases

| Use case | Rôle |
|----------|------|
| `GetTerracesUseCase` | Flow de terrasses + filtrage en mémoire via `FilterCriteria` |
| `GetTerraceDetailUseCase` | Terrasse par ID |
| `AddTerraceUseCase` | Validation + insertion |
| `UpdateTerraceUseCase` | Validation + mise à jour |
| `DeleteTerraceUseCase` | Suppression |
| `VoteTerraceUseCase` | Ajout d'un vote |

Le filtrage se fait en mémoire (`Flow.map { list.filter { ... } }`) — suffisant pour l'échelle MVP.

### 2.6 Navigation

| Route | Type | Description |
|-------|------|-------------|
| `"map"` | Écran principal | Carte + bottom sheet (liste / détail / filtre) |
| `"terrace/add?lat={lat}&lng={lng}"` | Plein écran | Formulaire d'ajout |
| `"terrace/{id}/edit"` | Plein écran | Formulaire d'édition |

Le sheet a 3 états gérés par le `MapViewModel` : `LIST`, `DETAIL`, `FILTER`.

### 2.7 Intégration osmdroid dans Compose

Wrapper `AndroidView` dans `ui/components/map/OsmMapView.kt` :
- `factory` : crée le `MapView`, configure les tuiles MAPNIK et le multi-touch
- `update` : diff les marqueurs par ID (pas de recréation complète), gère le camera move
- `DisposableEffect` : appelle `onResume()` / `onPause()` / `onDetach()`
- Marqueurs : cercles colorés selon le % de votes positifs

### 2.8 Modules Hilt

| Module | Scope | Fournit |
|--------|-------|---------|
| `DatabaseModule` | Singleton | `TerasseDatabase`, `TerraceDao`, `VoteDao` |
| `RepositoryModule` | Singleton | `TerraceRepository` → `TerraceRepositoryImpl` |
| `AppModule` | Singleton | `LocationProvider` (wrapper FusedLocationProviderClient) |

---

## 3. Structure des fichiers

```
com/terrass/app/
├── MainActivity.kt
├── TerassApplication.kt
├── data/
│   ├── local/
│   │   ├── TerasseDatabase.kt
│   │   ├── dao/
│   │   │   ├── TerraceDao.kt
│   │   │   └── VoteDao.kt
│   │   ├── entity/
│   │   │   ├── TerraceEntity.kt
│   │   │   ├── VoteEntity.kt
│   │   │   └── TerraceWithVotes.kt
│   │   └── mapper/
│   │       └── TerraceMapper.kt
│   ├── repository/
│   │   └── TerraceRepositoryImpl.kt
│   └── location/
│       └── LocationProvider.kt
├── domain/
│   ├── model/
│   │   ├── Terrace.kt
│   │   ├── Enums.kt
│   │   ├── FilterCriteria.kt
│   │   └── MapMarker.kt
│   ├── repository/
│   │   └── TerraceRepository.kt
│   └── usecase/
│       ├── GetTerracesUseCase.kt
│       ├── GetTerraceDetailUseCase.kt
│       ├── AddTerraceUseCase.kt
│       ├── UpdateTerraceUseCase.kt
│       ├── DeleteTerraceUseCase.kt
│       └── VoteTerraceUseCase.kt
├── di/
│   ├── AppModule.kt
│   ├── DatabaseModule.kt
│   └── RepositoryModule.kt
└── ui/
    ├── TerassApp.kt
    ├── theme/
    │   ├── Color.kt
    │   └── Theme.kt
    ├── components/
    │   ├── map/
    │   │   ├── OsmMapView.kt
    │   │   └── MapMarkerUtils.kt
    │   └── common/
    │       ├── VoteIndicator.kt
    │       └── AttributeChip.kt
    └── screens/
        ├── map/
        │   ├── MapScreen.kt
        │   ├── MapViewModel.kt
        │   └── components/
        │       ├── TerraceListContent.kt
        │       ├── TerraceListItem.kt
        │       ├── TerraceDetailSheet.kt
        │       └── FilterSheet.kt
        └── addterrace/
            ├── AddEditTerraceScreen.kt
            ├── AddEditTerraceViewModel.kt
            └── components/
                ├── SunExposureSection.kt
                ├── ComfortSection.kt
                ├── EnvironmentSection.kt
                ├── ServiceSection.kt
                └── LocationPickerMap.kt
```

---

## 4. Dépendances à ajouter

```toml
# gradle/libs.versions.toml
room = "2.6.1"
playServicesLocation = "21.3.0"

# libraries
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
play-services-location = { group = "com.google.android.gms", name = "play-services-location", version.ref = "playServicesLocation" }
```

---

## 5. Phases d'implémentation

| Phase | Contenu | Fichiers clés |
|-------|---------|---------------|
| **1. Data** | Enums, modèles domain, entities Room, DAOs, database, mappers, repository, DI modules | `domain/model/*`, `data/**`, `di/*` |
| **2. Carte** | Wrapper osmdroid Compose, init osmdroid, MapViewModel, localisation GPS, permissions | `ui/components/map/*`, `ui/screens/map/*`, `data/location/*` |
| **3. CRUD + Votes** | Use cases, formulaire ajout/édition, navigation, marqueurs sur carte, votes | `domain/usecase/*`, `ui/screens/addterrace/*`, `ui/TerassApp.kt` |
| **4. Liste + Filtres** | Bottom sheet liste, items, FilterSheet, FilterCriteria wiring | `ui/screens/map/components/*` |
| **5. Polish** | Reverse geocoding, icônes marqueurs custom, états vides, animations | Transversal |

---

## 6. Vérification

- [ ] Le build compile (`./gradlew assembleDebug`)
- [ ] La carte s'affiche et se déplace
- [ ] Long-press ouvre le formulaire d'ajout avec les bonnes coordonnées
- [ ] Une terrasse ajoutée apparaît comme marqueur sur la carte
- [ ] Le bottom sheet liste affiche les terrasses filtrées
- [ ] Les filtres réduisent la liste et les marqueurs
- [ ] Le vote pouce haut/bas met à jour le pourcentage affiché
- [ ] La suppression retire le marqueur de la carte
