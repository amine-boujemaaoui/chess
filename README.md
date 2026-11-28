# Chess

[![CI](https://github.com/amine-boujemaaoui/chess/actions/workflows/ci.yml/badge.svg)](https://github.com/amine-boujemaaoui/chess/actions/workflows/ci.yml)

Plateforme d'échecs multijoueur en temps réel, inspirée de Lichess en version réduite.

> Projet en cours de développement.

## Modules

| Module | Rôle |
|---|---|
| `engine` | Règles des échecs en Java pur, sans dépendance : coups légaux (roque, prise en passant, promotion), fin de partie (mat, pat, matériel insuffisant, règle des 50 coups, triple répétition), notations FEN, UCI et SAN, export PGN |
| `game-service` | Application Spring Boot : API REST, authentification JWT, PostgreSQL (migrations Flyway), documentation Swagger, erreurs au format `ProblemDetail` (RFC 9457) |
| `infra` | Docker Compose pour lancer PostgreSQL en local |

## Prérequis

- Java 21
- Docker, pour PostgreSQL en local et pour les tests d'intégration (Testcontainers)

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

## Lancer le service en local

```bash
cp infra/.env.example infra/.env     # puis remplir POSTGRES_PASSWORD et JWT_SECRET
docker compose -f infra/docker-compose.yml up -d
./mvnw install -DskipTests
./mvnw -pl game-service spring-boot:run
```

`game-service` lit les identifiants de la base dans `infra/.env`, comme Docker Compose. Une fois lancé :

- état du service : http://localhost:8080/actuator/health
- documentation de l'API (Swagger UI) : http://localhost:8080/swagger-ui/index.html

## Authentification

| Route | Accès | Rôle |
|---|---|---|
| `POST /api/auth/register` | public | crée un compte joueur (`username`, `password`) et renvoie un jeton |
| `POST /api/auth/login` | public | renvoie un jeton |
| `GET /api/players/me` | jeton | profil du joueur connecté |

Les routes protégées attendent l'en-tête `Authorization: Bearer <jeton>`. Le jeton est un JWT signé (HS256) avec `JWT_SECRET`, valable 24 heures. Les mots de passe sont hachés avec BCrypt. Les rôles sont `PLAYER`, attribué à l'inscription, et `ADMIN`, qui donne accès aux routes `/api/admin/**`.

Dans Swagger UI, le bouton « Authorize » permet de coller un jeton pour essayer les routes protégées.

## Licence

[MIT](LICENSE)
