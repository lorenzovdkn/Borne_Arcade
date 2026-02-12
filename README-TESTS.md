# Infrastructure de Tests

## Utilisation

```bash
# Lancer tous les tests
gradle test

# Compilation seule
gradle compileJava
```

## Structure

- `src/test/java/` - Tests JUnit 5
- `build.gradle` - Configuration simple avec MG2D inclus
- Tests ciblant les classes sans dépendances graphiques complexes

## Statut

✅ Infrastructure fonctionnelle
✅ MG2D intégrée au classpath
✅ JUnit 5 configuré