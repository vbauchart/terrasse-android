# Terrasse — Spécifications Fonctionnelles & Architecture Technique

## Contexte

Application Android de recensement et d'évaluation des terrasses de cafés et restaurants.
L'objectif est de permettre aux utilisateurs de découvrir, ajouter et noter des terrasses
selon des critères précis (soleil, bruit, confort...) via une carte interactive OpenStreetMap.

**Architecture** : offline-first avec synchronisation PocketBase self-hosted.
- Room = cache local (toujours disponible hors ligne)
- PocketBase = source de vérité distante (partagée entre tous les appareils)
- Identité anonyme par UUID d'appareil (pas de compte utilisateur, pas de Google)

---

## 1. Spécifications Fonctionnelles

### 1.1 Écran principal — Carte interactive
- TopAppBar avec titre "Terrasse", logo (parasol vectoriel), bouton hamburger
- Carte osmdroid (tuiles OpenStreetMap) sous la TopAppBar, entre barre de statut et barre de navigation
- Marqueurs colorés sur chaque terrasse (vert = bien noté, rouge = mal noté, gris = pas de vote)
- Tap sur un marqueur → bottom sheet de détail
- Long-press sur la carte → proposer d'ajouter une terrasse à cet endroit
- Bouton de recentrage sur la position GPS de l'utilisateur (spinner pendant la recherche, snackbar d'erreur, zoom quartier)
- FAB "+" pour ajouter une terrasse (GPS auto-détecté)
- FAB/icône filtre avec badge du nombre de filtres actifs
- Zoom par défaut : niveau ville (12.0), recentrage GPS : niveau quartier (15.0)
- Menu hamburger : Vue carte / Vue liste / Statut

### 1.2 Liste des terrasses (vue liste)
- Accessible via le menu hamburger "Vue liste"
- Liste `LazyColumn` : nom, % positif, 2-3 chips d'attributs principaux
- Tap sur un item → revient sur la carte + affiche le détail en bottom sheet

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
- Bouton "Rechercher l'établissement" → bottom sheet de recherche Photon
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
- Bottom sheet avec des chip groups par catégorie
- Chaque attribut est un `FilterChip` Material3
- Filtre supplémentaire : "Minimum X% positif"
- Bouton "Réinitialiser"
- Les filtres s'appliquent à la fois à la liste et aux marqueurs de la carte

### 1.6 Système de votes
- Pouce haut / pouce bas (binaire)
- **Un seul vote par appareil par terrasse** (identifié par `device_id`)
- Changer d'avis est possible : revoter dans le sens opposé inverse le compteur
- Revoter dans le même sens → no-op
- Affiché en pourcentage positif + nombre total
- "-" ou "Pas encore noté" si aucun vote

### 1.7 Recherche d'établissement (Photon)
- Bouton dans le formulaire d'ajout → bottom sheet de recherche
- Moteur : **Photon** (komoot, basé OSM) — supporte la recherche partielle/préfixe
- Résultats filtrés aux types food & drink OSM : restaurant, café, bar, pub, fast_food, food_court, biergarten, ice_cream
- Résultats restreints à la viewbox visible (bbox au format Photon : `minLon,minLat,maxLon,maxLat`)
- Résultats triés par distance croissante depuis le centre de la carte
- Affichage de l'adresse complète dans la liste résultats

### 1.8 Écran Statut
- Accessible depuis le menu hamburger
- Carte GPS : état NO_PERMISSION / SEARCHING / ACTIVE avec icône et couleur
- Carte Sync : état IDLE / SYNCING / UP_TO_DATE / OFFLINE avec icône et couleur
- Affiche l'URL PocketBase configurée

### 1.9 Modération (post-MVP)
- Champ `status` déjà présent en base ("active", "pending", "hidden")
- Le DAO filtre déjà sur `status = 'active'`
- Prêt pour un futur système de validation sans migration de schéma

---

## 2. Architecture Technique

### 2.1 Stack
- **Kotlin 2.1.20** + **Jetpack Compose** (Material3)
- **Hilt** (injection de dépendances)
- **Room** (base de données locale, cache offline-first)
- **PocketBase** (backend self-hosted, REST + SSE temps réel)
- **Navigation Compose** (routing)
- **osmdroid 6.1.20** (cartes OpenStreetMap, wrappé dans `AndroidView`)
- **play-services-location** (géolocalisation GPS)
- **Coroutines + Flow** (asynchrone et réactivité)
- **HttpURLConnection + org.json** (HTTP sans dépendance supplémentaire)

### 2.2 Architecture MVVM / Clean Architecture

```
UI (Compose) → ViewModel → UseCase → TerraceRepository (interface)
                                            ↓
                                TerraceRepositoryImpl
                                  ↙              ↘
                          Room (cache)    PocketBaseService (remote)
```

**Flux de données :**
- **Lectures** : Room (Flow réactif, toujours dispo offline)
- **Écriture** : local d'abord (optimistic), sync PocketBase async
- **Sync startup** : pull toutes les terrasses PocketBase → upsert Room
- **Temps réel** : SSE PocketBase → upsert Room → Flow Room notifie l'UI
- **Offline retry** : items `synced=false` poussés au prochain lancement

### 2.3 Schéma de base de données

**Table `terraces`** (version 4)
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
remote_id       TEXT        -- PocketBase record ID
synced          INTEGER     -- 0/1
thumbs_up       INTEGER DEFAULT 0
thumbs_down     INTEGER DEFAULT 0
```

**Table `votes`** (version 4)
```
id              INTEGER PRIMARY KEY AUTOINCREMENT
terrace_id      INTEGER REFERENCES terraces(id) ON DELETE CASCADE
is_positive     INTEGER     -- 0/1
device_id       TEXT        -- UUID appareil
created_at      INTEGER
remote_id       TEXT        -- PocketBase record ID
synced          INTEGER     -- 0/1

UNIQUE INDEX (terrace_id, device_id)  -- un seul vote par appareil par terrasse
```

### 2.4 Couche Domain — Modèles

- `Terrace` : modèle principal avec sous-objets `SunExposure`, `Comfort`, `Environment`, `Service` + `thumbsUp`, `thumbsDown`
- `FilterCriteria` : data class contenant tous les critères de filtre
- `SyncStatus` : enum `IDLE / SYNCING / UP_TO_DATE / OFFLINE`
- `PlaceResult` : résultat de recherche Photon
- Enums Kotlin : `SunTime`, `TerraceSize`, `NoiseLevel`, `ViewQuality`, `ServiceQuality`, `PriceRange`, `TerraceStatus`
- `TerraceRepository` : interface dans le domain, expose `syncStatus: StateFlow<SyncStatus>`

### 2.5 Use Cases

| Use case | Rôle |
|----------|------|
| `GetTerracesUseCase` | Flow de terrasses + filtrage en mémoire via `FilterCriteria` |
| `GetTerraceDetailUseCase` | Terrasse par ID |
| `AddTerraceUseCase` | Validation + insertion |
| `UpdateTerraceUseCase` | Validation + mise à jour |
| `DeleteTerraceUseCase` | Suppression |
| `VoteTerraceUseCase` | Vote (limité à 1 par device, changement de sens possible) |
| `SearchPlacesUseCase` | Recherche Photon avec bbox |

Le filtrage se fait en mémoire (`Flow.map { list.filter { ... } }`) — suffisant pour l'échelle MVP.

### 2.6 Navigation

| Route | Type | Description |
|-------|------|-------------|
| `"map"` | Écran principal | Carte + bottom sheet (liste / détail / filtre) |
| `"status"` | Plein écran | État GPS + état sync |
| `"terrace/add?lat={lat}&lng={lng}&zoom={zoom}"` | Plein écran | Formulaire d'ajout |
| `"terrace/{id}/edit"` | Plein écran | Formulaire d'édition |

### 2.7 Intégration osmdroid dans Compose

Wrapper `AndroidView` dans `ui/components/map/OsmMapView.kt` :
- `factory` : crée le `MapView`, configure les tuiles MAPNIK et le multi-touch
- `update` : `animateTo(center, zoom, 300ms)` pour recentrer/zoomer, recrée les marqueurs et overlays
- `DisposableEffect` : appelle `onResume()` / `onPause()` / `onDetach()`
- Marqueurs : cercles colorés selon le % de votes positifs
- `UserLocationOverlay` : point bleu/vert pour la position de l'utilisateur
- `MapEventsOverlay` : détection du long-press pour ajout

### 2.8 PocketBase — Collections

**terraces** (API rules : public read+write)
| Champ | Type |
|-------|------|
| name | text (required) |
| latitude / longitude | number |
| address, sun_times, size, road_proximity, noise_level, view_quality, service_quality, price_range, cuisine_type | text |
| is_covered, is_heated, has_vegetation | bool |
| status | text (default: "active") |
| device_id | text |
| thumbs_up / thumbs_down | number (default: 0) |

**votes** (API rules : public create+list)
| Champ | Type |
|-------|------|
| terrace_id | relation → terraces (cascadeDelete) |
| is_positive | bool |
| device_id | text |

Index unique sur `(terrace_id, device_id)` → 1 vote max par appareil par terrasse.

Compteurs `thumbs_up`/`thumbs_down` mis à jour via syntaxe PocketBase `"+1"` / `"-1"`.

### 2.9 Modules Hilt

| Module | Scope | Fournit |
|--------|-------|---------|
| `DatabaseModule` | Singleton | `TerasseDatabase`, `TerraceDao`, `VoteDao` |
| `RepositoryModule` | Singleton | `TerraceRepository` → `TerraceRepositoryImpl` |
| `AppModule` | Singleton | `LocationProvider` (wrapper FusedLocationProviderClient) |
| `NetworkModule` | Singleton | `CoroutineScope` (app-level, SupervisorJob) |

`DeviceIdProvider` et `PocketBaseService` s'injectent directement via `@Inject constructor`.

### 2.10 Configuration

`local.properties` (git-ignoré) :
```
pocketbaseUrl=http://192.168.x.x:8090
```

Lu par `app/build.gradle.kts` → `BuildConfig.POCKETBASE_URL`.

Debug uniquement : `app/src/debug/AndroidManifest.xml` avec `android:usesCleartextTraffic="true"` pour autoriser HTTP local.

---

## 3. Structure des fichiers

```
com/terrass/app/
├── MainActivity.kt                               ✅
├── TerassApplication.kt                          ✅
├── data/
│   ├── local/
│   │   ├── TerasseDatabase.kt                    ✅ (version 4)
│   │   ├── dao/
│   │   │   ├── TerraceDao.kt                     ✅
│   │   │   └── VoteDao.kt                        ✅
│   │   ├── entity/
│   │   │   ├── TerraceEntity.kt                  ✅ (+ remote_id, synced, thumbs)
│   │   │   └── VoteEntity.kt                     ✅ (+ device_id, remote_id, synced)
│   │   └── mapper/
│   │       └── TerraceMapper.kt                  ✅
│   ├── preferences/
│   │   └── DeviceIdProvider.kt                   ✅ (UUID persistant Android Keystore-ready)
│   ├── remote/
│   │   ├── PocketBaseService.kt                  ✅ (REST + SSE)
│   │   ├── PocketBaseConfig.kt                   ✅ (BuildConfig.POCKETBASE_URL)
│   │   ├── PhotonService.kt                      ✅ (recherche établissements)
│   │   └── dto/
│   │       └── TerraceDto.kt                     ✅
│   ├── repository/
│   │   └── TerraceRepositoryImpl.kt              ✅ (offline-first + sync)
│   └── location/
│       └── LocationProvider.kt                   ✅
├── domain/
│   ├── model/
│   │   ├── Terrace.kt                            ✅
│   │   ├── Enums.kt                              ✅
│   │   ├── PlaceResult.kt                        ✅
│   │   ├── FilterCriteria.kt                     ✅
│   │   └── SyncStatus.kt                         ✅
│   ├── repository/
│   │   └── TerraceRepository.kt                  ✅ (+ syncStatus: StateFlow)
│   └── usecase/
│       ├── GetTerracesUseCase.kt                 ✅
│       ├── GetTerraceDetailUseCase.kt            ✅
│       ├── AddTerraceUseCase.kt                  ✅
│       ├── UpdateTerraceUseCase.kt               ✅
│       ├── DeleteTerraceUseCase.kt               ✅
│       ├── VoteTerraceUseCase.kt                 ✅
│       └── SearchPlacesUseCase.kt                ✅ (Photon)
├── di/
│   ├── AppModule.kt                              ✅
│   ├── DatabaseModule.kt                         ✅
│   ├── NetworkModule.kt                          ✅ (appScope)
│   └── RepositoryModule.kt                       ✅
└── ui/
    ├── TerassApp.kt                              ✅
    ├── theme/
    │   ├── Color.kt                              ✅
    │   └── Theme.kt                              ✅
    ├── components/
    │   ├── map/
    │   │   └── OsmMapView.kt                     ✅
    │   └── common/
    │       └── VoteIndicator.kt                  ✅
    └── screens/
        ├── map/
        │   ├── MapScreen.kt                      ✅
        │   ├── MapViewModel.kt                   ✅
        │   └── components/
        │       ├── TerraceListContent.kt         ✅
        │       ├── TerraceListItem.kt            ✅
        │       ├── TerraceDetailSheet.kt         ✅
        │       └── FilterSheet.kt                ✅
        ├── status/
        │   ├── StatusScreen.kt                   ✅
        │   └── StatusViewModel.kt                ✅
        └── addterrace/
            ├── AddEditTerraceScreen.kt           ✅
            └── AddEditTerraceViewModel.kt        ✅

pb_migrations/
├── 1742760000_init.js                            ✅ (collections terraces + votes)
└── 1742820000_votes_unique_per_device.js         ✅ (index unique terrace_id+device_id)
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

### 5.2 Principe : MVP testable à chaque sprint

Chaque sprint produit un APK installable avec une fonctionnalité complète de bout en bout.
Le test de validation de chaque sprint est exécutable sur device ou émulateur.

---

## 6. Sprints d'implémentation

### Sprint 1 — La carte qui marche ✅
**Objectif** : L'app s'ouvre sur une carte OSM interactive centrée sur la position de l'utilisateur.

**Validation sur device** :
- [x] L'app s'ouvre sur une carte OpenStreetMap
- [x] La carte est interactive (zoom, pan)
- [x] La permission GPS est demandée
- [x] La carte se centre sur la position de l'utilisateur

---

### Sprint 2 — Ajouter une terrasse ✅
**Objectif** : L'utilisateur peut ajouter une terrasse (nom + position) et la voir apparaître comme marqueur sur la carte.

**Validation sur device** :
- [x] Long-press sur la carte ouvre le formulaire pré-rempli avec les coordonnées
- [x] FAB "+" ouvre le formulaire avec la position GPS
- [x] Remplir le nom + attributs et sauvegarder fonctionne
- [x] Le marqueur apparaît sur la carte après l'ajout

---

### Sprint 3 — Détail, édition, suppression et votes ✅
**Objectif** : L'utilisateur peut consulter une terrasse, la modifier, la supprimer et voter.

**Validation sur device** :
- [x] Tap sur un marqueur affiche le bottom sheet de détail
- [x] Pouce haut/bas incrémente le compteur et met à jour le %
- [x] Modifier ouvre le formulaire pré-rempli, la sauvegarde met à jour le marqueur
- [x] Supprimer (avec confirmation) retire le marqueur de la carte
- [x] La couleur du marqueur change selon le % de votes

---

### Sprint 3b — Recherche d'établissement ✅
**Objectif** : L'utilisateur peut rechercher un café/restaurant et importer ses données dans le formulaire.

Service : **Photon** (komoot) — recherche partielle/préfixe native, pas de dépendance supplémentaire.

**Validation sur device** :
- [x] Chercher "Pizza Lu" trouve "Pizza Luigi" dans la viewbox
- [x] Résultats filtrés aux types food & drink OSM
- [x] Résultats triés par distance depuis le centre de la carte
- [x] Adresse complète affichée dans la liste résultats
- [x] Tap sur un résultat pré-remplit le formulaire

---

### Sprint 4 — Liste et filtres ✅
**Objectif** : L'utilisateur peut voir la liste des terrasses et filtrer par attributs.

**Validation sur device** :
- [x] Vue liste accessible depuis le menu hamburger
- [x] Chaque item affiche nom, % positif, chips d'attributs
- [x] Les filtres réduisent la liste ET les marqueurs sur la carte
- [x] Le badge indique le nombre de filtres actifs

---

### Sprint 5 — Polish et finitions ✅
**Objectif** : L'app est agréable à utiliser avec des finitions soignées.

**Validation sur device** :
- [x] L'adresse s'affiche dans le détail quand disponible
- [x] Les marqueurs ont des icônes colorées distinctes
- [x] Les états vides sont affichés correctement
- [x] Les transitions sont fluides (slideInHorizontally + fadeIn)

---

### Sprint 6 — Collaboration PocketBase ✅
**Objectif** : Les terrasses et votes sont partagés entre tous les appareils via PocketBase self-hosted.

**Contenu** :
- `DeviceIdProvider` : UUID stable par appareil (SharedPreferences)
- `PocketBaseService` : REST (CRUD terrasses + votes) + SSE (temps réel)
- `TerraceDto` + `TerraceMapper.toEntity()` (DTO → Room)
- `TerraceRepositoryImpl` : offline-first, sync au démarrage, SSE continu, retry exponentiel
- `SyncStatus` : enum IDLE / SYNCING / UP_TO_DATE / OFFLINE
- `StatusScreen` + `StatusViewModel` : état GPS + état sync
- `NetworkModule` : `CoroutineScope` app-level (SupervisorJob)
- DB version 3 : `remote_id`, `synced`, `thumbs_up`, `thumbs_down` sur `TerraceEntity`
- `BuildConfig.POCKETBASE_URL` via `local.properties` (git-ignoré)
- `app/src/debug/AndroidManifest.xml` : cleartext HTTP pour dev local
- `docker-compose.yml` + `dev-local.sh` (détecte IP LAN, build + install APK)
- Migrations PocketBase via `pb_migrations/` (équivalent Flyway)

**Bugfixes inclus** :
- `updateTerrace` préserve `thumbsUp`/`thumbsDown`/`remoteId` depuis l'entité existante

**Validation sur device** :
- [x] Ajouter une terrasse sur un appareil → apparaît sur un autre via SSE
- [x] Votes synchronisés entre appareils
- [x] StatusScreen affiche GPS actif + sync UP_TO_DATE
- [x] Hors ligne → status OFFLINE, données locales disponibles
- [x] Retour en ligne → sync automatique au redémarrage

---

### Sprint 7 — Vote unique par appareil ✅
**Objectif** : Un appareil ne peut voter qu'une fois par terrasse (changement de sens autorisé).

**Contenu** :
- `VoteEntity` : ajout `device_id`, index unique `(terrace_id, device_id)`
- `VoteDao` : `getByTerraceAndDevice(terraceId, deviceId)`
- `TerraceRepositoryImpl.vote()` : logique idempotente (même sens → no-op, sens opposé → swap compteurs)
- `PocketBaseService` : `addVote` retourne le `remoteId`, nouveau `updateVote` + `patchTerraceCounter`
- Migration PocketBase `1742820000_votes_unique_per_device.js` : nettoyage doublons + index unique
- DB version 4

**Validation sur device** :
- [x] Voter deux fois dans le même sens → compteur n'augmente pas
- [x] Voter dans le sens opposé → les deux compteurs s'ajustent correctement
- [x] Index unique respecté côté PocketBase

---

## 7. Sécurité — Notes

### Situation actuelle
L'API PocketBase est publique (rules vides). Pas de secret dans l'APK.

### Approche recommandée (post-MVP)

**Play Integrity API + JWT Android Keystore**

Flux :
1. Au 1er lancement, l'app demande un token d'attestation à la **Play Integrity API**
2. Ce token (signé par Google) prouve que l'APK est légitime, non modifié, sur un appareil sain
3. L'app envoie ce token au backend → le backend le vérifie auprès des serveurs Google
4. Si valide, le backend génère un **JWT** et le retourne → stocké dans l'**Android Keystore** (enclave matérielle, non-extractible)
5. Tous les appels suivants incluent `Authorization: Bearer <jwt>`
6. Les **API rules PocketBase** passent à `@request.auth.id != ""` (auth requise)

Garanties :
- APK signé avec la bonne clé de signature (non repackagé)
- Appareil non rooté, Play Store actif
- Aucun secret statique dans l'APK
- Le JWT est device-specific et non-extractible

Limites :
- Nécessite **Google Play Services** sur l'appareil (~95% des Android)
- Nécessite un projet Google Cloud (gratuit jusqu'à 10 000 requêtes/jour)
- Ne protège pas contre un utilisateur sur émulateur certifié ou appareil rooté avec bypass

Complément recommandé :
- **Rate limiting** par IP via Caddy/nginx devant PocketBase (protection complémentaire)
