# Commandes utiles — Terrass

## Build & test
```bash
# Tests unitaires
./gradlew test

# Build APK debug
./gradlew assembleDebug

# Les deux enchaînés
./gradlew test assembleDebug
```

**Important** : Java 21 est configuré comme défaut système via `archlinux-java`.
Ne jamais préfixer avec `JAVA_HOME=...`, utiliser `./gradlew` directement.

## Linting / formatting
Pas de lint/format automatique configuré explicitement — le projet suit les conventions Kotlin standard.

## Git
```bash
git status / git diff / git log
git add <fichiers> && git commit -m "message"
```
