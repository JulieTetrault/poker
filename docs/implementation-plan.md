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
→ Domain factory rehydration
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

Create `Game`, `Shoe`, `Deck`, `Card`, `Player`, and `Hand`, plus `Suit` and
`Rank` enums, following the domain model.

Domain classes contain business state and behavior only.

Do not add:

- Spring annotations
- JPA annotations
- Hibernate annotations
- Jackson annotations
- UUID-generation logic

to domain models.

Core relationships:

- A `Game` owns one `Shoe` and its players.
- Each `Player` belongs to one game and owns one `Hand`.
- Each `Shoe` belongs to one game.
- A `Shoe` contains zero or more attached decks.
- A new `Deck` contains 52 cards and has a null `shoeId` until attached.
- Once attached, a deck cannot be removed from its shoe.
- A `Card` has its own UUID and retains the `deckId` of its original deck.
- A card's numeric value is derived from `Rank.getValue()`.
- A player's hand value is derived from the cards in `Hand`.
- Keep dealing, remaining-card counts, and shuffling in `Shoe`.
- Keep hand totals and hand-related behavior in `Hand`.
- Keep player ordering behavior associated with `Game`.
- Track undealt cards independently from each deck's original 52-card
  collection.
- Removing a player discards that player's cards. They remain dealt and are
  never returned to the shoe.

Constructors must receive already-created identifiers rather than generate
identifiers internally.

For example:

```java
public Deck(UUID id, UUID shoeId, List<Card> cards) {
    this.id = Objects.requireNonNull(id, "id");
    this.shoeId = shoeId;
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

At minimum:

```text
GameFactory
DeckFactory
```

If `Player` is treated as independently created through the application layer,
use a `PlayerFactory` as well.

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

        List<Card> cards = generateCards(deckId);

        return new Deck(
            deckId,
            null,
            cards
        );
    }
}
```

The factory creates the deck ID and all 52 card IDs.

A `GameFactory` should create:

- the game ID
- the shoe ID
- the initial empty shoe
- the initial empty player collection

For example:

```java
public Game create(String name) {
    UUID gameId = idGenerator.nextId();
    UUID shoeId = idGenerator.nextId();

    Shoe shoe = new Shoe(
        shoeId,
        gameId,
        List.of()
    );

    return new Game(
        gameId,
        name,
        shoe,
        List.of()
    );
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

### 2. Separate creation from rehydration

Loading an existing aggregate from persistence must not generate new IDs or
apply "new object" initialization rules.

Factories should therefore distinguish:

```text
create(...)
```

from:

```text
rehydrate(...)
```

or:

```text
restore(...)
```

For example:

```java
public Deck rehydrate(
    UUID id,
    UUID shoeId,
    List<Card> cards
) {
    return new Deck(id, shoeId, cards);
}
```

Rehydration:

- preserves persisted IDs
- preserves persisted card state
- does not generate cards
- does not generate UUIDs
- does not reset the shoe relationship
- does not perform creation-only behavior

Persistence mappers should use this rehydration path instead of calling a
creation factory.

### 3. Create API DTOs and API mappers

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

### 4. Create persistence entities

Create a persistence representation separate from the domain model.

Use JPA/Hibernate annotations only on persistence entities.

Typical persistence entities:

- `GameEntity`
- `ShoeEntity`
- `DeckEntity`
- `CardEntity`
- `PlayerEntity`
- `HandEntity`

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
→ ShoeEntity
→ DeckEntity
→ CardEntity

GameEntity
→ PlayerEntity
→ HandEntity
```

Cards held in a player's hand still retain their original deck identity.

Choose a persistence representation for card state that clearly distinguishes:

- undealt cards
- cards currently held by players
- discarded cards belonging to removed players

The persistence representation does not need to mirror the domain object graph
exactly as long as persistence mapping preserves domain invariants.

### 5. Create persistence mappers

Create explicit mappers between domain models and persistence entities.

Persistence mappers must not invoke aggregate creation logic.

When reading from the database, they must use the appropriate domain factory's
rehydration mechanism.

For example:

```text
DeckEntity
→ DeckPersistenceMapper
→ DeckFactory.rehydrate(...)
→ Deck
```

and:

```text
GameEntity
→ GamePersistenceMapper
→ GameFactory.rehydrate(...)
→ Game
```

Examples:

`GamePersistenceMapper`

- `GameEntity toEntity(Game game)`
- `Game toDomain(GameEntity entity)`

`DeckPersistenceMapper`

- `DeckEntity toEntity(Deck deck)`
- `Deck toDomain(DeckEntity entity)`

`PlayerPersistenceMapper`

- `PlayerEntity toEntity(Player player)`
- `Player toDomain(PlayerEntity entity)`

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
→ Domain factory rehydration
→ Domain model
```

Avoid sharing persistence entities outside the persistence package.

### 6. Create repository abstractions

Define repository interfaces independently from Spring Data.

Repository interfaces should operate on domain aggregate roots rather than
Hibernate entities.

For example:

```java
public interface GameRepository {
    Game save(Game game);
    Optional<Game> findById(UUID id);
    void deleteById(UUID id);
}
```

and:

```java
public interface DeckRepository {
    Deck save(Deck deck);
    Optional<Deck> findById(UUID id);
}
```

The application/domain layer depends on these interfaces, not directly on
Spring Data or Hibernate.

Inside the persistence layer, create Spring Data repositories that operate on
JPA entities:

```java
interface JpaGameRepository extends JpaRepository<GameEntity, UUID> {
}
```

Then provide persistence adapters implementing the domain repository
interfaces:

```text
GameRepository
    ↑
HibernateGameRepository
    ├── JpaGameRepository
    └── GamePersistenceMapper
```

A persistence adapter should:

1. receive a domain aggregate
2. map it to a JPA entity
3. persist it through Spring Data
4. map the persisted entity back through the rehydration path when required

Repository adapters do not create new aggregate identities.

### 7. Configure H2 in-memory persistence

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

### 8. Create application services

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
    return gameRepository.save(game);
}
```

The application service must not call `UUID.randomUUID()` itself when identity
generation is already owned by a factory.

Likewise:

```java
public Deck createDeck() {
    Deck deck = deckFactory.create();
    return deckRepository.save(deck);
}
```

Services resolve resources, enforce ownership constraints, invoke domain
behavior, and persist modified aggregates.

Use transactions for operations involving multiple persistent changes.

### 9. Create controllers

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

### 10. Implement domain behavior

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
- physical card identity

### 11. Add centralized error handling

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

### 12. Test and verify

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
- all generated card IDs are unique
- every card receives the correct original `deckId`
- a newly created deck has a null `shoeId`
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
- dealt cards retain original `deckId`
- removing a player discards their hand
- discarded cards never return to the shoe

#### Persistence tests

Using H2, verify:

- domain aggregates round-trip through persistence without changing IDs
- persistence mappers rehydrate rather than recreate aggregates
- relationships are persisted correctly
- unassigned decks retain a null `shoeId`
- attaching a deck persists ownership
- a deck cannot be attached to multiple shoes
- deleting a game cascades only to resources owned by that game

### 13. Build and verification workflow

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
- Creation and persistence rehydration are separate factory operations.
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
- Hand value is derived from `Hand`; it is not stored on `Player`.
- Cards retain their original deck identity after being dealt.
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
