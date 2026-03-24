# Terrasse — Spécifications Fonctionnelles & Architecture Technique

## Contexte

Application Android de recensement et d'évaluation des terrasses de cafés et restaurants.
L'objectif est de permettre aux utilisateurs de découvrir, ajouter et noter des terrasses
selon des critères précis (soleil, bruit, confort...) via une carte interactive OpenStreetMap.

**MVP** : stockage local uniquement (Room), pas d'authentification, pas de backend.

---

## 1. Spécifications Fonctionnelles

### 1.1 Écran principal — Carte interactive
- TopAppBar avec titre "Terrasse", logo (parasol vectoriel), bouton hamburger (prévu pour menu latéral)
- Carte osmdroid (tuiles OpenStreetMap) sous la TopAppBar, entre barre de statut et barre de navigation
- Marqueurs colorés sur chaque terrasse (vert = bien noté, rouge = mal noté, gris = pas de vote)
- Tap sur un marqueur → bottom sheet de détail
- Long-press sur la carte → proposer d'ajouter une terrasse à cet endroit
- Bouton de recentrage sur la position GPS de l'utilisateur (spinner pendant la recherche, snackbar d'erreur, zoom quartier)
- FAB "+" pour ajouter une terrasse (GPS auto-détecté)
- FAB/icône filtre avec badge du nombre de filtres actifs
- Zoom par défaut : niveau ville (12.0), recentrage GPS : niveau quartier (15.0)

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
| Ensoleillée | Choix multiple | Matin, Midi, Soir |

**Confort & équipement**
| Attribut | Type | Valeurs |
|----------|------|---------|
| Couverte | Booléen | Oui / Non |
| Chauffée | Booléen | Oui / Non |
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
- **Kotlin 2.1.20** + **Jetpack Compose** (Material3)
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
sun_times       TEXT        -- "morning,noon,evening" (comma-separated)
is_covered      INTEGER     -- 0/1
is_heated       INTEGER     -- 0/1
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
- Enums Kotlin : `SunTime`, `TerraceSize`, `NoiseLevel`, `ViewQuality`, `ServiceQuality`, `PriceRange`, `TerraceStatus`
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
- `update` : `animateTo(center, zoom, 300ms)` pour recentrer/zoomer, recrée les marqueurs et overlays
- `DisposableEffect` : appelle `onResume()` / `onPause()` / `onDetach()`
- Marqueurs : cercles colorés selon le % de votes positifs
- `UserLocationOverlay` : point bleu/vert pour la position de l'utilisateur
- `MapEventsOverlay` : détection du long-press pour ajout

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
├── MainActivity.kt                         ✅
├── TerassApplication.kt                    ✅
├── data/
│   ├── local/
│   │   ├── TerasseDatabase.kt              ✅
│   │   ├── dao/
│   │   │   ├── TerraceDao.kt               ✅
│   │   │   └── VoteDao.kt                  ✅
│   │   ├── entity/
│   │   │   ├── TerraceEntity.kt            ✅
│   │   │   ├── VoteEntity.kt               ✅
│   │   │   └── TerraceWithVotes.kt         ✅
│   │   └── mapper/
│   │       └── TerraceMapper.kt            ✅
│   ├── remote/
│   │   └── NominatimService.kt             ✅
│   ├── repository/
│   │   └── TerraceRepositoryImpl.kt        ✅
│   └── location/
│       └── LocationProvider.kt             ✅
├── domain/
│   ├── model/
│   │   ├── Terrace.kt                      ✅
│   │   ├── Enums.kt                        ✅
│   │   ├── PlaceResult.kt                  ✅
│   │   ├── FilterCriteria.kt               ✅
│   │   └── MapMarker.kt                    Sprint 5
│   ├── repository/
│   │   └── TerraceRepository.kt            ✅
│   └── usecase/
│       ├── GetTerracesUseCase.kt           ✅
│       ├── GetTerraceDetailUseCase.kt      ✅
│       ├── AddTerraceUseCase.kt            ✅
│       ├── UpdateTerraceUseCase.kt         ✅
│       ├── DeleteTerraceUseCase.kt         ✅
│       ├── VoteTerraceUseCase.kt           ✅
│       └── SearchPlacesUseCase.kt          ✅
├── di/
│   ├── AppModule.kt                        ✅
│   ├── DatabaseModule.kt                   ✅
│   └── RepositoryModule.kt                 ✅
└── ui/
    ├── TerassApp.kt                        ✅
    ├── theme/
    │   ├── Color.kt                        ✅
    │   └── Theme.kt                        ✅
    ├── components/
    │   ├── map/
    │   │   └── OsmMapView.kt               ✅
    │   └── common/
    │       └── VoteIndicator.kt            ✅
    └── screens/
        ├── map/
        │   ├── MapScreen.kt                ✅
        │   ├── MapViewModel.kt             ✅
        │   └── components/
        │       ├── TerraceListContent.kt   ✅
        │       ├── TerraceListItem.kt      ✅
        │       ├── TerraceDetailSheet.kt   ✅
        │       └── FilterSheet.kt          ✅
        └── addterrace/
            ├── AddEditTerraceScreen.kt     ✅
            └── AddEditTerraceViewModel.kt  ✅

res/
└── drawable/
    └── ic_logo.xml                         ✅ (logo vectoriel parasol)
```

---

## 4. Dépendances

> **Note** : ne pas accepter les bumps automatiques d'Android Studio vers AGP 9.x / Kotlin 2.2.x
> qui cassent la compatibilité Hilt+KSP. Rester sur les versions stables ci-dessous.

```toml
# gradle/libs.versions.toml — versions actuelles
agp = "8.8.2"
kotlin = "2.1.20"
ksp = "2.1.20-1.0.32"
hilt = "2.53.1"
room = "2.6.1"
osmdroid = "6.1.20"
playServicesLocation = "21.3.0"
coroutines = "1.9.0"
junit5 = "5.10.2"
turbine = "1.2.0"
mockk = "1.13.13"
```

Test runtime : `junit-platform-launcher` requis par Gradle 9.x.

---

## 5. Règles de développement

### 5.1 Tests unitaires obligatoires

Chaque classe de haut niveau doit avoir des tests unitaires associés :

| Couche | Classes testées | Framework |
|--------|----------------|-----------|
| Domain | Tous les use cases, `FilterCriteria.matches()`, modèles (votePercentage, etc.) | JUnit 5 + Coroutines Test |
| Data | DAOs (Room in-memory), Mappers, `TerraceRepositoryImpl` | JUnit 5 + Room Testing |
| UI | ViewModels (états, actions) | JUnit 5 + Turbine (Flow testing) |

Structure des tests :
```
app/src/test/java/com/terrass/app/          -- tests unitaires (JVM)
app/src/androidTest/java/com/terrass/app/   -- tests instrumentés (Room)
```

Dépendances de test à ajouter :
```toml
junit5 = "5.10.2"
coroutines-test = "1.9.0"
turbine = "1.2.0"
mockk = "1.13.13"
room-testing = "2.6.1"
```

### 5.2 Principe : MVP testable à chaque sprint

Chaque sprint produit un APK installable avec une fonctionnalité complète de bout en bout.
Le test de validation de chaque sprint est exécutable sur device ou émulateur.

---

## 6. Sprints d'implémentation

### Sprint 1 — La carte qui marche ✅
**Objectif** : L'app s'ouvre sur une carte OSM interactive centrée sur la position de l'utilisateur.

**Contenu** :
- Init osmdroid dans `TerassApplication`
- Wrapper Compose `OsmMapView` (AndroidView + lifecycle)
- `MapViewModel` avec gestion de la position GPS
- `LocationProvider` (wrapper FusedLocationProviderClient)
- Demande de permission localisation
- DI : `AppModule` fournit `LocationProvider`

**Tests unitaires** :
- `MapViewModelTest` : état initial, mise à jour position, gestion permission refusée

**Validation sur device** :
- [x] L'app s'ouvre sur une carte OpenStreetMap
- [x] La carte est interactive (zoom, pan)
- [x] La permission GPS est demandée
- [x] La carte se centre sur la position de l'utilisateur
- [x] Le build compile (`./gradlew assembleDebug`)
- [x] Les tests passent (`./gradlew test`)

---

### Sprint 2 — Ajouter une terrasse ✅
**Objectif** : L'utilisateur peut ajouter une terrasse (nom + position) et la voir apparaître comme marqueur sur la carte.

**Contenu** :
- Enums (`Enums.kt`), modèles domain (`Terrace.kt`, `SunExposure`, `Comfort`, `Environment`, `Service`)
- Entities Room (`TerraceEntity`, `VoteEntity`, `TerraceWithVotes`)
- `TerraceDao`, `VoteDao`, `TerasseDatabase`
- `TerraceMapper` (entity ↔ domain)
- `TerraceRepository` (interface) + `TerraceRepositoryImpl`
- `AddTerraceUseCase`, `GetTerracesUseCase`
- DI : `DatabaseModule`, `RepositoryModule`
- `AddEditTerraceScreen` + `AddEditTerraceViewModel` (formulaire avec les 4 sections d'attributs)
- Navigation : route `"terrace/add?lat={lat}&lng={lng}"`
- Long-press sur la carte → naviguer vers le formulaire d'ajout
- FAB "+" → ajouter depuis position GPS
- Affichage des marqueurs sur la carte depuis la base de données

**Tests unitaires** :
- `TerraceMapperTest` : conversion entity → domain et domain → entity
- `AddTerraceUseCaseTest` : validation nom vide, insertion OK
- `GetTerracesUseCaseTest` : retourne les terrasses, flow réactif
- `AddEditTerraceViewModelTest` : validation du formulaire, sauvegarde
- `TerraceDao` tests instrumentés : insert, getAll, getById

**Validation sur device** :
- [x] Long-press sur la carte ouvre le formulaire pré-rempli avec les coordonnées
- [x] FAB "+" ouvre le formulaire avec la position GPS
- [x] Remplir le nom + attributs et sauvegarder fonctionne
- [x] Le marqueur apparaît sur la carte après l'ajout
- [x] Retour en arrière depuis le formulaire annule l'ajout
- [x] Les tests passent (`./gradlew test`)

---

### Sprint 3 — Détail, édition, suppression et votes ✅
**Objectif** : L'utilisateur peut consulter une terrasse, la modifier, la supprimer et voter.

**Contenu** :
- `GetTerraceDetailUseCase`, `UpdateTerraceUseCase`, `DeleteTerraceUseCase`, `VoteTerraceUseCase`
- `TerraceDetailSheet` (bottom sheet de détail avec tous les attributs)
- Tap sur marqueur → affiche le détail en bottom sheet
- Boutons pouce haut / pouce bas dans le détail
- `VoteIndicator` composant (affiche "82% positif · 17 avis")
- Bouton modifier → navigation vers `"terrace/{id}/edit"` (réutilise `AddEditTerraceScreen`)
- Bouton supprimer avec confirmation dialog
- Couleur des marqueurs selon le % de votes (vert/rouge/gris)
- TopAppBar avec logo vectoriel (parasol + table + soleil) et bouton hamburger
- Feedback visuel sur recentrage GPS (spinner + snackbar d'erreur)
- Animation `animateTo` sur la carte lors du recentrage

**Tests unitaires** (32 tests, 0 failures) :
- `VoteTerraceUseCaseTest` : insertion vote thumbs up / thumbs down
- `UpdateTerraceUseCaseTest` : mise à jour OK, validation nom vide
- `DeleteTerraceUseCaseTest` : suppression OK
- `GetTerraceDetailUseCaseTest` : terrasse avec votes agrégés, ID inconnu
- `Terrace.votePercentage` : test des cas limites (0 votes, 100%, 0%)
- `MapViewModelTest` : recentrage sans permission, avec position (zoom 15.0), sans position, dismiss erreur

**Validation sur device** :
- [x] Tap sur un marqueur affiche le bottom sheet de détail
- [x] Les attributs sont affichés correctement
- [x] Pouce haut/bas incrémente le compteur et met à jour le %
- [x] Modifier ouvre le formulaire pré-rempli, la sauvegarde met à jour le marqueur
- [x] Supprimer (avec confirmation) retire le marqueur de la carte
- [x] La couleur du marqueur change selon le % de votes
- [x] Bouton recentrage GPS fonctionne avec feedback visuel et zoom quartier
- [x] Les tests passent (`./gradlew test`)

---

### Sprint 3b — Recherche d'établissement (Nominatim) ✅
**Objectif** : L'utilisateur peut rechercher un café/restaurant par son nom et importer automatiquement son nom, adresse et coordonnées GPS dans le formulaire d'ajout.

**Contenu** :
- `PlaceResult` modèle domain
- `NominatimService` (appel HTTP `HttpURLConnection` + parse `org.json`, sans dépendance supplémentaire)
- `SearchPlacesUseCase` (wraps le service, retourne `Result<List<PlaceResult>>`)
- `AddEditTerraceViewModel` : `searchQuery`, `searchResults`, `isSearching`, `searchError`, debounce 500ms, `applyPlaceResult()`
- `AddEditTerraceScreen` : bouton "Rechercher l'établissement" + `ModalBottomSheet` (champ recherche, liste résultats, états chargement/erreur/vide)
- Headers Nominatim : `User-Agent: Terrass Android App`, `Accept-Language: fr`

**Tests unitaires** :
- `SearchPlacesUseCaseTest` : résultats OK, propagation d'erreur, liste vide
- `AddEditTerraceViewModelTest` : `applyPlaceResult` pré-remplit nom/lat/lng/adresse, nettoie l'état de recherche

**Validation sur device** :
- [x] Chercher "Café de Flore Paris" affiche des résultats
- [x] Tap sur un résultat pré-remplit le formulaire (nom, coordonnées, adresse)
- [x] Ferme le sheet après sélection
- [x] Les tests passent (`./gradlew test`)

---

### Sprint 4 — Liste et filtres ✅
**Objectif** : L'utilisateur peut voir la liste des terrasses et filtrer par attributs.

**Contenu** :
- `BottomSheetScaffold` intégré dans `MapScreen`
- `TerraceListContent` (LazyColumn) + `TerraceListItem`
- `AttributeChip` composant réutilisable
- `FilterCriteria` modèle + logique de filtrage dans `GetTerracesUseCase`
- `FilterSheet` avec chip groups par catégorie
- Badge nombre de filtres actifs sur l'icône filtre
- Tap sur un item de la liste → zoom sur le marqueur + affiche détail
- Les filtres masquent aussi les marqueurs de la carte

**Tests unitaires** :
- `FilterCriteria.matches()` : tous les critères individuels + combinaisons
- `GetTerracesUseCaseTest` : filtrage en mémoire (chaque attribut, combinaisons)
- `MapViewModelTest` : changement de filtres met à jour la liste et les marqueurs

**Validation sur device** :
- [x] Swipe-up depuis le peek affiche la liste des terrasses
- [x] Le peek affiche le nombre de terrasses ("12 terrasses")
- [x] Chaque item affiche nom, % positif, chips d'attributs
- [x] Tap sur un item zoom sur le marqueur et affiche le détail
- [x] Les filtres réduisent la liste ET les marqueurs sur la carte
- [x] Le badge indique le nombre de filtres actifs
- [x] Réinitialiser les filtres restaure la liste complète
- [x] Les tests passent (`./gradlew test`)

---

### Sprint 5 — Polish et finitions ✅
**Objectif** : L'app est agréable à utiliser avec des finitions soignées.

**Contenu** :
- Reverse geocoding pour afficher l'adresse (Android Geocoder, best-effort)
- Icônes marqueurs custom (cercles colorés avec graduation)
- États vides ("Aucune terrasse" / "Aucun résultat pour ces filtres")
- Animations de transition (sheet, navigation)
- Bouton de recentrage GPS sur la carte
- Gestion des erreurs et edge cases (pas de GPS, pas de réseau pour les tuiles)
- Revue générale et nettoyage du code

**Tests unitaires** :
- Tests des edge cases : pas de réseau, permission refusée, base vide

**Validation sur device** :
- [x] L'adresse s'affiche dans le détail quand disponible
- [x] Les marqueurs ont des icônes colorées distinctes (pin + logo)
- [x] Les états vides sont affichés correctement ("Aucune terrasse" / "Aucun résultat pour ces filtres")
- [x] L'app reste fonctionnelle sans réseau (tuiles en cache, données locales)
- [x] Les transitions sont fluides (slideInHorizontally + fadeIn)
- [x] Les tests passent (`./gradlew test`)
