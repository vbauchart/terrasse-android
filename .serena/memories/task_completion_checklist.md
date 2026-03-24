# Checklist fin de tâche — Terrass

1. `./gradlew test` → tous les tests passent
2. `./gradlew assembleDebug` → BUILD SUCCESSFUL
3. Pas de `JAVA_HOME=...` dans les commandes Gradle
4. Vérifier que les nouvelles classes Hilt utilisent `@Inject constructor()` quand possible (pas de `@Provides` superflu)
5. Pas de migration Room si le champ `address: String?` existait déjà dans le modèle
