# API implementation plan

Status: implementation steps with confirmed API decisions, September 29, 2026.

Game endpoints are not yet implemented. Follow the [requirements](requirements.md),
[domain model](domain.md), and [OpenAPI contract](openapi.json). Use the
[manual testing guide](manual-api-testing.md) when the API is available.

## Architecture

Keep the HTTP API, application layer, domain model, and persistence model
separated.

The creation flow is:

```text
HTTP request
→ Request DTO
→ API mapper / application command
→ Application service
→ Domain factory
→ Aggregate root
→ Repository interface
→ Persistence mapper
→ JPA/Hibernate entity
→ H2 database
```

The read/rehydration flow is:

```text
H2 database
→ JPA/Hibernate entity
→ Persistence mapper
→ Aggregate root
→ Application service
→ Response mapper
→ Response DTO
→ HTTP response
```

Domain classes must remain independent of Spring, Hibernate, JPA, Jackson,
and transport-specific concerns.

Aggregate roots must not generate their own identifiers. New aggregate roots
are created through domain factories, which are responsible for identity
generation and valid initial state.

Use the following conceptual package structure:

```text
com.example.poker
├── api
│   ├── controller
│   ├── request
│   ├── response
│   └── mapper
├── application
│   └── service
├── domain
│   ├── factory
│   ├── model
│   ├── repository
│   └── service
└── persistence
    ├── entity
    ├── mapper
    └── repository
```

Persistence classes should be referred to as JPA/Hibernate entities rather
than DTOs. DTOs are reserved for API transport models.

## Implementation steps

### 1. Create domain models and factories

Create `Game`, `Shoe`, `Deck`, `Card`, and `Player`, plus `Suit` and
`Rank` enums, following the domain model.

Domain classes contain business state and behavior only.

`Game`, `Player`, and `Deck` are aggregate roots. `Shoe` remains a domain
model inside Game so it can own shoe-specific behavior; it is not a root.

Do not add:

- Spring annotations
- JPA annotations
- Hibernate annotations
- Jackson annotations
- UUID-generation logic

to domain models.

Core relationships:

- A `Game` owns one `Shoe` and its players.
- Each `Player` belongs to one game and owns an ordered `List<Card> cards`.
- Each `Shoe` belongs to one game and has no identifiers of its own.
- A `Shoe` contains zero or more attached decks.
- A new `Deck` contains 52 cards and has a null `gameId` until attached.
- Once attached, a deck cannot be removed from its shoe.
- A `Card` is an immutable value object containing only suit and rank.
  It has no UUID or deck reference; lists preserve duplicate occurrences.
- A card's numeric value is derived from `Rank.getValue()`.
- A player's hand value is derived from `Player.cards`.
- Keep dealing, remaining-card counts, and shuffling in `Shoe`.
- Keep card receipt and hand-total calculations in `Player`.
- Keep player ordering behavior associated with `Game`.
- Track undealt cards independently from each deck's original 52-card
  collection.
- Removing a player discards that player's cards. They remain dealt and are
  never returned to the shoe.

Constructors must receive already-created identifiers rather than generate
identifiers internally.

For example:

```java
public Deck(UUID id, List<Card> cards) {
    this.id = Objects.requireNonNull(id, "id");
    this.cards = List.copyOf(cards);
}
```

Do not write:

```java
public Deck() {
    this.id = UUID.randomUUID();
}
```

Identity generation belongs outside the aggregate.

#### Domain factories

Create explicit factories for aggregate-root creation.

Create:

```text
GameFactory
PlayerFactory
DeckFactory
```

The aggregate roots are `Game`, `Player`, and `Deck`; use a creation factory
for each. `Shoe` is an internal domain model owned by Game. It has no
ShoeFactory, persistence entity, or repository. The Game constructor initializes it.

Factories are responsible for:

- generating identifiers
- creating required child entities
- enforcing valid initial aggregate state
- preventing callers from constructing partially initialized aggregates

For example:

```java
public final class DeckFactory {

    private final IdGenerator idGenerator;

    public DeckFactory(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public Deck create() {
        UUID deckId = idGenerator.nextId();

        List<Card> cards = generateCards();

        return new Deck(deckId, cards);
    }
}
```

The factory creates the deck ID and all 52 suit/rank card values.

A `GameFactory` should create:

- the game ID
- the initial empty shoe
- the initial empty player collection

For example:

```java
public Game create(String name) {
    UUID gameId = idGenerator.nextId();

    return new Game(gameId, name);
}
```

Use an identifier abstraction such as:

```java
public interface IdGenerator {
    UUID nextId();
}
```

with a production implementation:

```java
public final class UuidGenerator implements IdGenerator {

    @Override
    public UUID nextId() {
        return UUID.randomUUID();
    }
}
```

This keeps UUID generation outside the aggregate and makes factories easy to
test deterministically.

### 2. Create persistence entities, mappers, and rehydration paths

Implement the three entities and corresponding persistence mappers in this step.
Cards are persistence values, not entities.
Use `toEntity` for domain-to-entity mapping and `fromEntity` for entity-to-domain
mapping. Keep aggregate creation separate from restoration.

#### Separate creation from rehydration

Loading an existing aggregate from persistence must not generate new IDs or
apply "new object" initialization rules.

Domain factories are responsible only for creating new aggregates. Do not add
`rehydrate` or `restore` methods to factories.

Persistence mappers own rehydration through `fromEntity`. They instantiate
domain models using persisted identifiers and restore child collections and
relationships directly, without invoking factories or identity generators.

For example, in `DeckEntityMapper`:

```java
public Deck fromEntity(DeckEntity entity) {
    Deck deck = new Deck(
        entity.getId(),
        entity.getCards().stream().map(cardEntityMapper::fromEntity).toList()
    );
    deck.setGameId(entity.getGameId());
    return deck;
}
```

Rehydration:

- preserves persisted IDs
- preserves persisted card state
- does not generate cards
- does not generate UUIDs
- does not reset the shoe relationship
- does not perform creation-only behavior

Persistence mappers implement this rehydration path in `fromEntity`.

#### Persistence entities

Create a persistence representation separate from the domain model.

Use JPA/Hibernate annotations only on persistence entities.

Typical persistence entities:

- `GameEntity`
- `DeckEntity`
- `PlayerEntity`

These entities may contain persistence-specific details such as:

- `@Entity`
- `@Table`
- `@Id`
- `@OneToOne`
- `@OneToMany`
- `@ManyToOne`
- cascade rules
- fetch strategy
- database column details

Do not copy these annotations into domain classes.

The persistence relationships should preserve the same ownership rules as the
domain:

```text
GameEntity
→ DeckEntity
→ CardValue[] (JSON column)

GameEntity
→ PlayerEntity
→ CardValue[] (JSON column)
```

Cards in a player's list contain suit/rank values only. Repeated faces remain
separate list occurrences.

When implementing dealing and shuffling in the domain-behavior step, extend
the persistence representation to clearly distinguish:

- undealt cards
- cards currently held by players
- discarded cards belonging to removed players

The persistence representation does not need to mirror the domain object graph
exactly as long as persistence mapping preserves domain invariants.

#### Persistence mappers

Create explicit mappers between domain models and persistence entities.
Register entity mappers as Spring components with instance `toEntity` and
`fromEntity` methods. Use constructor injection: DeckEntityMapper and
PlayerEntityMapper depend on CardEntityMapper; GameEntityMapper depends on
DeckEntityMapper and PlayerEntityMapper. Future repositories inject these mappers.

Persistence mappers must not invoke aggregate creation logic.

When reading from the database, `fromEntity` constructs domain models directly
and restores persisted state. Factories are not involved.

For example:

```text
DeckEntity
→ DeckEntityMapper
→ Deck
```

and:

```text
GameEntity
→ GameEntityMapper
→ Game
```

Examples:

`GameEntityMapper`

- `GameEntity toEntity(Game game)`
- `Game fromEntity(GameEntity entity)`

`DeckEntityMapper`

- `DeckEntity toEntity(Deck deck)`
- `Deck fromEntity(DeckEntity entity)`

`PlayerEntityMapper`

- `PlayerEntity toEntity(Player player)`
- `Player fromEntity(PlayerEntity entity)`

Keep mapping logic out of domain classes.

The persistence write flow is:

```text
Domain model
→ Persistence mapper
→ Hibernate entity
→ database
```

The persistence read flow is:

```text
Database
→ Hibernate entity
→ Persistence mapper
→ Domain model
```

Avoid sharing persistence entities outside the persistence package.

#### Current implementation scope

Entities and mappers preserve the state currently exposed by the domain models:
identifiers, names, deck attachment, original deck cards, player order, and hands.
Player owns its cards directly; there is no Hand class or Hand entity/table.
DeckEntity and PlayerEntity each store ordered
card arrays in a `cards` column. `CardEntity` contains only suit and rank with no identity or deck reference. `CardListConverter` uses JPA `AttributeConverter`
to encode the list as JSON text in that row; there is no Card entity/table or
card join table. Array order and repeated values are preserved. Enums are
encoded as names and derived numeric values are not stored. JPA does not define
a portable native SQL array mapping for structured card values; this conversion
keeps the array in the owning row without database-specific array types.

Mapper `fromEntity` methods restore supplied state without generating IDs.
CardValueMapper maps immutable values with `toValue` and `fromValue`.
The mappings use scalar ownership IDs and unidirectional JPA associations to
avoid recursive mapping. GameEntity stores ordered `decks` directly, not a
ShoeEntity association. Shoe has no identifiers. GameEntityMapper restores it
from the game entity's deck collection, and Deck.gameId records the owning game.
Ordered entity collections use join tables, including `game_decks`.
Standalone decks retain a nullable game ID. Removing a player deletes only its
stored card array; the deck's original values remain unchanged.

The current Shoe model does not expose undealt order or discarded state. Add
that state and extend the persistence mappings alongside the domain-behavior
step; it cannot yet be restored by these mappers. JPA API is a production
dependency, with Hibernate and H2 used only in tests at this step. Runtime
persistence configuration remains a later step. Map loaded collections while
the persistence context is open; collection associations are lazy by default.

### 3. Create aggregate repositories

Define Spring-independent `JPAGameRepository`, `JPADeckRepository`, and
`JPAPlayerRepository` interfaces in the domain layer. Expose `create` and `update`
operations returning the persisted aggregate. Game and Player additionally expose
`deleteById(UUID id)`; Deck does not expose deletion.

Implement persistence adapters using Spring Data JPA repositories and the existing
entity mappers. Updates and deletions must first check whether the aggregate exists
and throw the custom domain `NotFoundException` with the aggregate type and ID
when it does not. Missing updates must never create an aggregate.

Resolve Player and attached Deck game relationships using a JPA reference.
Unattached decks must persist with a null game relationship. Keep each write
operation transactional, and preserve existing aggregate identities.

Use fixture-based Mockito unit tests for successful writes and missing aggregate
failures, including confirmation that rejected operations perform no writes.
Add the JPA runtime and embedded H2 dependencies required to wire the repositories;
explicit database configuration remains in step 5.

### 4. Create API DTOs and API mappers

Create separate request and response DTOs matching the OpenAPI contract.

DTOs describe the external HTTP contract. They are not domain classes and are
not persistence entities.

Request DTOs:

- `AddPlayerRequest`
- `AttachDeckRequest`
- `CreateGameRequest`
- `DealRequest`

Response DTOs should use the `Response` suffix convention and remain
alphabetically organized in OpenAPI.

API mappers must not generate UUIDs or directly instantiate aggregate roots.

For aggregate creation, API mappers should map transport data into application
inputs.

For example:

```text
CreateGameRequest
→ CreateGameCommand / name
→ GameService
→ GameFactory.create(name)
```

rather than:

```text
CreateGameRequest
→ new Game(...)
```

A mapper may still create simple value objects or non-aggregate transport
representations where appropriate.

Response mapping remains:

```text
Domain model
→ Response mapper
→ Response DTO
```

For example:

```text
Game
→ GameApiMapper
→ CreateGameResponse
```

Validate required fields, UUIDs, nonblank names of at most 100 characters, and
positive deal counts.

Reject unknown fields and incorrect JSON types rather than coercing them.

### 5. Configure H2 in-memory persistence

Use H2 as the local in-memory database with Hibernate/JPA.

The database may be recreated on application restart. Persistence across
application restarts is not required by the assignment.

Use Spring Boot JPA configuration so schema creation is automatic during local
development and tests.

Keep the selected database behind repository abstractions so replacing H2 with
PostgreSQL or another database would not require changes to the domain model.

Database configuration belongs in infrastructure/application configuration,
not in domain classes.

Do not write domain behavior into repository classes.

### 6. Create application services

Application services orchestrate:

- factories
- repositories
- domain behavior
- transactions

For creation operations:

```text
Controller
→ Request DTO
→ Application service
→ Domain factory
→ Repository
```

For example:

```java
public Game createGame(CreateGameRequest request) {
    Game game = gameFactory.create(request.name());
    return gameRepository.create(game);
}
```

The application service must not call `UUID.randomUUID()` itself when identity
generation is already owned by a factory.

Likewise:

```java
public Deck createDeck() {
    Deck deck = deckFactory.create();
    return deckRepository.create(deck);
}
```

Services resolve resources, enforce ownership constraints, invoke domain
behavior, and persist modified aggregates.

Use transactions for operations involving multiple persistent changes.

### 7. Create controllers

Create game, deck, and player controllers using the routes below.

Controllers:

- parse HTTP input
- validate HTTP input
- delegate to application services
- map returned domain objects to response DTOs
- return documented HTTP statuses

Controllers must not:

- generate UUIDs
- instantiate aggregate roots
- contain domain logic
- access JPA repositories directly
- work with Hibernate entities

All paths below begin with `/api/v1`.

| Method | Path | Operation | Success |
| --- | --- | --- | --- |
| POST | `/games` | Create game | 201 |
| DELETE | `/games/{gameId}` | Delete game | 204 |
| POST | `/decks` | Create deck | 201 |
| POST | `/games/{gameId}/decks` | Add deck to game | 204 |
| GET | `/games/{gameId}/decks/cards` | Count undealt cards by suit and rank | 200 |
| GET | `/games/{gameId}/decks/suits/cards` | Count undealt cards by suit | 200 |
| POST | `/games/{gameId}/decks/shuffle` | Shuffle undealt cards | 204 |
| POST | `/games/{gameId}/players` | Add player | 201 |
| GET | `/games/{gameId}/players` | List players by descending hand value | 200 |
| DELETE | `/games/{gameId}/players/{playerId}` | Remove player | 204 |
| GET | `/games/{gameId}/players/{playerId}/cards` | Retrieve player's cards | 200 |
| POST | `/games/{gameId}/players/{playerId}/cards/deal` | Deal cards to player | 200 |

Adding a deck succeeds with `204 No Content`; no attachment response DTO is
required.

### 8. Implement domain behavior

Implement:

- game creation
- deck creation
- deck attachment
- game deletion
- player membership
- dealing
- hand retrieval
- player ordering
- both undealt-card counts
- shuffle

Return players ordered by:

1. hand value descending
2. player UUID ascending for equal hand totals

Delete everything owned by a deleted game, including:

- its shoe
- attached decks
- cards
- players
- hands

Unassigned decks remain outside the game.

When removing a player:

- remove the player from active game membership
- discard all cards in their hand
- do not return those cards to the shoe
- discarded cards cannot be dealt again

A deck belongs to at most one shoe at a time. A shoe belongs to exactly one
game.

Deal:

```text
min(requested count, available cards)
```

Example:

```json
{
  "dealtCards": 10,
  "remainingCards": 0
}
```

Count only undealt cards.

Suit ordering:

1. hearts
2. spades
3. clubs
4. diamonds

Within each suit:

1. King
2. Queen
3. Jack
4. 10
5. ...
6. 2
7. Ace

Implement Fisher–Yates directly using a random number generator.

Do not call a library-provided shuffle implementation.

Shuffle only undealt cards and preserve:

- cards already held in hands
- discarded cards
- card counts
- suit/rank values and occurrence counts

### 9. Add centralized error handling

Use a centralized REST exception handler.

Use:

```text
ErrorResponse
- detail
- status
- code
```

Example:

```json
{
  "detail": "Deck '...' is already assigned to a game.",
  "status": 409,
  "code": "DECK_ALREADY_ASSIGNED"
}
```

Do not expose:

- stack traces
- Hibernate exceptions
- database details
- internal exception messages

Clients should use `status` and `code` rather than parsing `detail`.

### 10. Test and verify

Use JUnit Jupiter and AssertJ for domain and service tests.

Prefer spec-style organization using:

- `@Nested`
- `@DisplayName`

Use Mockito where collaboration needs isolation.

Factories must be tested independently.

#### Factory tests

Verify:

- aggregate roots receive generated IDs
- IDs are generated by `IdGenerator`, not the aggregate
- `GameFactory` creates a game and shoe with the correct relationship
- `DeckFactory` creates exactly 52 cards
- all 52 card faces are distinct within a deck
- cards contain suit/rank values without identifiers
- a newly created deck has a null `gameId`
- deterministic test ID generators can be substituted

For example, tests should be able to provide a predictable generator rather
than depending on `UUID.randomUUID()`.

#### Rehydration tests

Verify:

- persisted aggregate IDs are preserved
- rehydration does not generate new IDs
- rehydration does not regenerate cards
- rehydration preserves shoe attachment
- rehydration preserves dealt/discarded state

#### Domain tests

Verify:

- 52 unique cards per deck
- Ace-to-King values
- hand totals
- player ordering
- deck attachment invariants
- Fisher–Yates preserves every card
- dealing removes cards only from the undealt shoe
- dealing preserves card values and their occurrence counts
- removing a player discards their hand
- discarded cards never return to the shoe

#### Persistence tests

Keep entity tests in separate `GameEntityTest`, `DeckEntityTest`, and
`PlayerEntityTest` files.
Test card-array conversion separately in `CardListConverterTest`.
Name mapper tests after their `*EntityMapper` class.

Using H2, verify:

- domain aggregates round-trip through persistence without changing IDs
- persistence mappers rehydrate rather than recreate aggregates
- relationships are persisted correctly
- unassigned decks retain a null `gameId`
- attaching a deck persists ownership
- a deck cannot be attached to multiple shoes
- deleting a game cascades only to resources owned by that game

### 11. Build and verification workflow

After Java changes:

```sh
sdk env
./mvnw spotless:apply
./mvnw validate
./mvnw verify
```

Keep:

- OpenAPI schemas
- controllers
- DTOs
- factories
- domain model
- persistence entities
- persistence mappers
- manual API examples

aligned with implemented behavior.

## Confirmed decisions

- Aggregate roots do not generate their own UUIDs.
- Aggregate roots are created through explicit domain factories.
- UUID generation is abstracted behind an `IdGenerator`.
- Factories create new aggregates; persistence mappers own rehydration through `fromEntity`.
- Rehydration preserves existing identities and state.
- API mappers do not instantiate aggregate roots directly.
- Persistence mappers do not invoke new-aggregate creation logic.
- Domain classes contain no Spring, JPA, Hibernate, or Jackson annotations.
- API DTOs, domain models, and persistence entities are separate models.
- Repository interfaces operate on domain aggregates.
- Spring Data repositories and JPA entities remain internal to the persistence
  layer.
- H2 is used as the in-memory local database.
- Card value is derived from `Rank`; it is not stored on `Card`.
- Hand value is derived from `Player.cards`; it is not persisted.
- Cards contain suit/rank only; ordered collections preserve duplicate faces.
- Removing a player discards their cards without returning them to the shoe.
- A deck belongs to only one shoe at a time.
- A shoe belongs to exactly one game.
- Attached decks cannot be removed.
- Adding a deck returns `204 No Content`.
- `ErrorResponse` contains `detail`, `status`, and `code`.

## Remaining proposals

Canonical initial/appended card order remains a proposal.

The API error media type still needs one final decision:

- retain `application/problem+json` with the simplified `ErrorResponse`, or
- use `application/json`

This is an API-contract decision and does not add a business requirement.
