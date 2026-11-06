# people-service

Kotlin Spring Boot service that owns **cast and creators** (people). Each person has a name, one or more roles (`ACTOR`, `DIRECTOR`, `WRITER`, `CREATOR`), and an optional birth date, bio and photo URL.

- **GraphQL subgraph** (`/graphql`): `person`, `people` (search by name, filter by role), `createPerson`, `updatePerson`, `deletePerson`. It's one of the two subgraphs that the Apollo Router combines into the API used by `movie-web`. `Person` is a federation entity (`@key(fields: "id")`), so the router can fetch people by id when a movie's credits ask for them.
- **gRPC server** (`PeopleService`): used by `movie-service` to check a person before adding a credit (`GetPerson`)
- **gRPC client** of `movie-service`: `DeleteCreditsByPerson`, to remove a deleted person's credits

The gRPC contracts are in the `protos` submodule (at `protos/`).

| RPC | Purpose |
|---|---|
| `GetPerson` | Fetch one person by id |
| `ListPeople` | List everyone |
| `SearchPeople` | Case-insensitive name search |
| `BatchGetPeople` | Fetch many people by id in one call |
| `CreatePerson` / `UpdatePerson` / `DeletePerson` | Manage people |

gRPC errors are returned as status codes (`NOT_FOUND`, `INVALID_ARGUMENT`), and GraphQL errors as `NOT_FOUND` / `BAD_REQUEST`.

**Deleting a person** removes them locally, then calls movie-service to delete their credits, inside one transaction. If movie-service can't be reached, the call fails and the delete is rolled back. This applies to both the GraphQL mutation and the gRPC `DeletePerson` RPC.

Data is stored in an H2 file database under `./data/`, with the schema managed by Flyway.

## Prerequisites

- **JDK 21** (pinned in `.tool-versions`; with [asdf](https://asdf-vm.com/): `asdf install`)
- **movie-service** running, for deleting people (see its README)

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

| Port | What |
|---|---|
| `8081` | GraphQL (`POST /graphql`), GraphiQL (http://localhost:8081/graphiql) |
| `9090` | gRPC server |

Server reflection is enabled, so you can explore the gRPC API with [grpcurl](https://github.com/fullstorydev/grpcurl):

```bash
grpcurl -plaintext localhost:9090 list
grpcurl -plaintext -d '{"name": "keanu"}' localhost:9090 people.v1.PeopleService/SearchPeople
```

To start with an empty database, stop the service and delete the `data/` folder.

## Tests

```bash
./gradlew test
```

Tests run with the `test` profile (set in `build.gradle.kts`). It uses an in-memory H2 database and a random gRPC port, so the tests don't touch `./data/` and can run while the service is running. The movie-service gRPC client is mocked where a test deletes a person.

The HTML report is written to `build/reports/tests/test/index.html`.
