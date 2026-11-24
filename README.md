# Chess

[![CI](https://github.com/amine-boujemaaoui/chess/actions/workflows/ci.yml/badge.svg)](https://github.com/amine-boujemaaoui/chess/actions/workflows/ci.yml)

Plateforme d'échecs multijoueur en temps réel, inspirée de Lichess en version réduite.

> Projet en cours de développement.

## Modules

| Module | Rôle |
|---|---|
| `engine` | Règles des échecs en Java pur, sans dépendance : coups légaux (roque, prise en passant, promotion), fin de partie (mat, pat, matériel insuffisant, règle des 50 coups, triple répétition), notations FEN, UCI et SAN, export PGN |

## Prérequis

- Java 21

Maven n'a pas besoin d'être installé : le projet fournit le wrapper Maven (`mvnw`).

## Compiler et lancer les tests

```bash
./mvnw verify        # Linux, macOS
mvnw.cmd verify      # Windows
```

Les tests perft les plus profonds (marqués `slow`, une quinzaine de secondes) sont exclus par défaut. La CI les lance toujours ; pour les lancer en local :

```bash
./mvnw verify -DexcludedGroups=
```

## Licence

[MIT](LICENSE)
