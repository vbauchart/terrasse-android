# Terrasse

Application Android de recensement et d'évaluation des terrasses de cafés et restaurants.
Carte interactive OpenStreetMap, notation collaborative, backend PocketBase self-hosted.

---

## Prérequis

- **Android Studio** Hedgehog ou plus récent
- **JDK 21** (`org.gradle.java.home` configuré dans `local.properties`)
- **Docker** + **Docker Compose** (pour PocketBase)
- **adb** (optionnel, pour installation directe sur téléphone)

---

## 1. Builder le projet

### Depuis Android Studio

1. Ouvrir le dossier racine du projet dans Android Studio
2. Laisser la synchronisation Gradle se terminer
3. **Build → Make Project** ou `Ctrl+F9`

### Depuis la ligne de commande

```bash
./gradlew assembleDebug
# APK généré : app/build/outputs/apk/debug/app-debug.apk
```

---

## 2. Lancer PocketBase

```bash
docker compose up -d
```

L'interface d'administration est disponible sur **http://localhost:8090/_/**

Pour arrêter :

```bash
docker compose down
```

Les données sont persistées dans `pb_data/` (gitignorée).

---

## 3. Schéma PocketBase — migrations

Les collections sont créées **automatiquement au démarrage** de PocketBase via le système
de migrations intégré (dossier `pb_migrations/`). Aucune action manuelle dans l'UI n'est nécessaire.

```
pb_migrations/
└── 1742760000_init.js   ← crée les collections terraces + votes
```

PocketBase applique les fichiers dans l'ordre de leur timestamp (préfixe du nom de fichier),
exactement comme Flyway. Les migrations déjà appliquées sont enregistrées dans `pb_data/`
et ne sont jamais rejouées.

### Ajouter une migration

Créer un fichier `pb_migrations/<timestamp>_<description>.js` :

```js
/// <reference path="../pb_data/types.d.ts" />

migrate((db) => {
    const dao = new Dao(db);
    const collection = dao.findCollectionByNameOrId("terraces");

    collection.schema.addField(new SchemaField({
        name: "opening_hours",
        type: "text",
    }));

    return dao.saveCollection(collection);
}, (db) => {
    // rollback optionnel
});
```

Redémarrer PocketBase suffit à appliquer la migration :

```bash
docker compose restart pocketbase
```

### Référence des collections

<details>
<summary>Collection <code>terraces</code> (API rules : toutes vides = public)</summary>

| Champ | Type | Options |
|-------|------|---------|
| `name` | Text | Required |
| `latitude` | Number | Required |
| `longitude` | Number | Required |
| `address` | Text | |
| `sun_times` | Text | |
| `is_covered` | Bool | |
| `is_heated` | Bool | |
| `size` | Text | |
| `road_proximity` | Text | |
| `noise_level` | Text | |
| `view_quality` | Text | |
| `has_vegetation` | Bool | |
| `service_quality` | Text | |
| `price_range` | Text | |
| `cuisine_type` | Text | |
| `status` | Text | |
| `device_id` | Text | |
| `thumbs_up` | Number | |
| `thumbs_down` | Number | |

</details>

<details>
<summary>Collection <code>votes</code> (API rules : toutes vides = public)</summary>

| Champ | Type | Options |
|-------|------|---------|
| `terrace_id` | Relation → terraces | Required, cascade delete |
| `is_positive` | Bool | |
| `device_id` | Text | |

</details>

---

## 4. Lancer le script de build avec l'URL locale

Le script détecte automatiquement l'IP de la machine sur le réseau local, met à jour
`local.properties`, build l'APK et l'installe sur le téléphone s'il est branché en USB.

```bash
./dev-local.sh
```

**Prérequis :** PocketBase doit être démarré (`docker compose up -d`) avant de lancer le script.

Ce que fait le script :
1. Détecte l'IP LAN (ex: `192.168.1.42`)
2. Écrit `pocketbaseUrl=http://192.168.1.42:8090` dans `local.properties`
3. Build l'APK debug via Gradle
4. Si un téléphone est détecté en ADB → `adb install -r`

### Installation manuelle de l'APK

Si le téléphone n'est pas branché, installer l'APK généré manuellement :

```
app/build/outputs/apk/debug/app-debug.apk
```

### Utilisation depuis Android Studio après le script

Le script met à jour `local.properties` avec la bonne IP.
Dans Android Studio : **File → Sync Project with Gradle Files**, puis builder/lancer normalement.

---

## Lancer les tests

```bash
./gradlew test
```
