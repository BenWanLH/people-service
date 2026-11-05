# people-service

Kotlin Spring Boot service that owns **cast and creators** (people). Each person has a name, one or more roles (`ACTOR`, `DIRECTOR`, `WRITER`, `CREATOR`), and an optional birth date, bio and photo URL.

It exposes a **gRPC API only**, defined in `people/v1/people.proto` in the `protos` submodule (at `protos/`). Its only client is `movie-service`, which calls it to look people up, search them and manage them on behalf of the web app.

| RPC | Purpose |
|---|---|
| `GetPerson` | Fetch one person by id |
| `ListPeople` | List everyone |
| `SearchPeople` | Case-insensitive name search |
| `BatchGetPeople` | Fetch many people by id in one call (used to resolve movie credits) |
| `CreatePerson` / `UpdatePerson` / `DeletePerson` | Manage people |

Errors are returned as gRPC status codes (`NOT_FOUND`, `INVALID_ARGUMENT`).

Data is stored in an H2 file database under `./data/`, with the schema managed by Flyway.

## Prerequisites

- **JDK 21** (pinned in `.tool-versions`; with [asdf](https://asdf-vm.com/): `asdf install`)

Gradle comes with the project (`./gradlew`), so it doesn't need installing.

## Clone

```bash
git clone --recurse-submodules https://github.com/BenWanLH/people-service.git
```

If you already cloned without submodules, run `git submodule update --init`. Without it, `protos/` is empty and the build fails.

## Run

```bash
./gradlew bootRun
```

The gRPC server listens on **`localhost:9090`**. Start this service before `movie-service`.

Server reflection is enabled, so you can explore it with [grpcurl](https://github.com/fullstorydev/grpcurl):

```bash
grpcurl -plaintext localhost:9090 list
grpcurl -plaintext -d '{"name": "keanu"}' localhost:9090 people.v1.PeopleService/SearchPeople
```

To start with an empty database, stop the service and delete the `data/` folder.

## Tests

```bash
./gradlew test
```

Tests run with the `test` profile (set in `build.gradle.kts`). It uses an in-memory H2 database and a random gRPC port, so the tests don't touch `./data/` and can run while the service is running.

The HTML report is written to `build/reports/tests/test/index.html`.
