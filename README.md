# Chess

Plateforme d'échecs multijoueur en temps réel, inspirée de Lichess en version réduite.

> Projet en cours de développement.

## Modules

| Module | Rôle |
|---|---|
| `engine` | Règles des échecs en Java pur, sans dépendance |

## Prérequis

- Java 21

Maven n'a pas besoin d'être installé : le projet fournit le wrapper Maven (`mvnw`).

## Compiler et lancer les tests

```bash
./mvnw verify        # Linux, macOS
mvnw.cmd verify      # Windows
```

## Licence

[MIT](LICENSE)
