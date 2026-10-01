# API implementation plan

Status: implementation steps with confirmed API decisions, September 30, 2026.

Game and deck endpoints, request validation, and centralized exception handling
are implemented. Follow the [requirements](requirements.md),
[domain model](domain.md), and [OpenAPI contract](openapi.json).

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
├── service
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

`Game`, `Player`, and `Deck` are aggregate roots. Game owns one Shoe, which keeps
undealt cards and delegates to CardDealer, CardCounter, and
CardShuffler. Shoe is a domain model without an independent identity.

Do not add:

- Spring annotations
- JPA annotations
- Hibernate annotations
- Jackson annotations
- UUID-generation logic

to domain models.

Core relationships:

- A `Game` owns its shoe and players; Shoe owns undealt cards.
- Each `Player` belongs to one game and owns an ordered `List<Card> cards`.
- A new `Deck` holds only identity and a null `gameId` until attached.
  `Deck.generateCards()` creates the standard 52 cards when Shoe adds it.
- Once attached, a deck cannot be removed from its game.
- A `Card` is an immutable value object containing only suit and rank.
  It has no UUID or deck reference; lists preserve duplicate occurrences.
- A card's numeric value is derived from `Rank.getValue()`.
- A player's hand value is derived from `Player.cards`.
- Keep dealing, remaining-card counts, and shuffling in `Shoe`, delegating to
  CardDealer, CardCounter, and CardShuffler.
- Keep card receipt and hand-total calculations in `Player`.
- Keep player listing order in `GameService.getPlayers`.
- Track undealt cards independently from each deck's original 52-card
  collection.
- Removing a player discards that player's cards. They remain dealt and are
  never returned to the undealt list.

Constructors must receive already-created identifiers rather than generate
identifiers internally.

For example:

```java
public Deck(UUID id) {
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
for each. Game initializes its Shoe; Shoe has no separate factory, entity, or
repository. Shoe provides default card collaborators and supports full injection
for tests; counters are owned by individual shoes.

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


        return new Deck(deckId);
    }
}
```

The factory creates the deck ID and all 52 suit/rank card values.

A `GameFactory` should create:

- the game ID
- the initial empty deck and undealt-card lists
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

Persistence mappers restore domain state using persisted identifiers and ordered
card lists. DeckEntityMapper restores only deck identity and ownership;
no mapper depends on a factory or generates identities.

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
`fromEntity` methods. DeckEntityMapper has no collaborators.
PlayerEntityMapper depends on CardEntityMapper;
GameEntityMapper depends on CardEntityMapper and PlayerEntityMapper.
Repositories inject these mappers.

Persistence mappers must not generate new identities or invoke factories.

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
identifiers, names, deck attachment, undealt card order, and hands.
Player owns its cards directly; there is no Hand class or Hand entity/table.
DeckEntity stores only ID and game attachment. PlayerEntity stores ordered
card arrays in a `cards` column; GameEntity stores ordered `undealt_cards`. `CardEntity` contains only suit and rank with no identity or deck reference. `CardListConverter` uses JPA `AttributeConverter`
to encode the list as JSON text in that row; there is no Card entity/table or
card join table. Array order and repeated values are preserved. Enums are
encoded as names and derived numeric values are not stored. JPA does not define
a portable native SQL array mapping for structured card values; this conversion
keeps the array in the owning row without database-specific array types.

Mapper `fromEntity` methods restore supplied state without generating IDs.
CardValueMapper maps immutable values with `toValue` and `fromValue`.
The mappings use scalar ownership IDs and unidirectional JPA associations to
avoid recursive mapping. GameEntityMapper restores Shoe from the ordered
undealt-card list, then passes it and restored players into Game. Deck.gameId
records ownership through decks.game_id; there is no game_decks join table.
Standalone decks retain a nullable game ID. Removing a player discards their
stored cards without changing the game's undealt list.

GameEntity persists undealt_cards as JSON using CardListConverter. Restoration
preserves exact remaining order and exhaustion without rebuilding from decks.
Map loaded collections while the persistence context is open.

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
explicit database configuration remains in step 6.

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

Names use the Boot-managed validation starter, @Valid, @NotBlank, and
@Size(max = 100), with explicit field-specific constraint messages.

GameController explicitly calls UUIDValidator for body deck IDs before
application services. DealCardsRequest uses @Valid and Jakarta @NotNull,
@Min(1), and @Max(Integer.MAX_VALUE) on Integer count. Path IDs use @PathVariable UUID arguments
and Spring conversion; conversion errors return 400 BAD_REQUEST.
Count constraints reject missing/null and nonpositive counts. Strict Jackson
integer binding rejects fractions, strings, booleans, and overflow, which the
central mapper reports with a count-specific detail.
For body IDs, UUIDValidator requires standard 36-character hexadecimal UUID text;
CardCountValidator has been removed. There is no custom UUID deserializer,
Spring binding editor, or Jackson exception-path inspection.

UUIDValidator throws RequestValidationException, centrally mapped to
400 BAD_REQUEST with its public detail. Spring name constraint failures
also return 400 BAD_REQUEST. Jackson still rejects unknown properties,
trailing JSON values, and non-string names; unreadable bodies return
400 BAD_REQUEST. Existing 404, 422, and 500 mappings remain.

### 5. Create application services

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
public Game createGame(String name) {
    Game game = gameFactory.create(name);
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

#### Current service scope (implemented)

- GameService: createGame(String name), deleteGame(UUID gameId),
  addDeck(UUID gameId, UUID deckId), addPlayer(UUID gameId, String playerName),
  removePlayer(UUID gameId, UUID playerId), getPlayerCards(UUID gameId, UUID playerId),
  getPlayers(UUID gameId), shuffleCards(UUID gameId), getGame(UUID gameId),
  dealCards(int cardCount, UUID gameId, UUID playerId),
  returning the cards actually dealt.
- DeckService: createDeck(), attachDeckToGame(UUID gameId, UUID deckId).
- PlayerService: createPlayer(UUID gameId, String name),
  getPlayer(UUID gameId, UUID playerId).

Services use constructor injection and transactions. Creation uses domain
factories. Game membership writes go through GameRepository.update; the mapper
sets child game relationships and cascade mappings persist the changes.
Player creation prepares domain objects; deck attachment persists ownership.
Player lookups require a matching game ID. An attached deck cannot be attached
again. Player removal uses orphan removal; game deletion explicitly deletes
attached decks and cascades to players.

GameService resolves the game first, checks player ownership, and delegates to
Game.dealCards(int cardCount, Player player). Game coordinates removing
min(cardCount, remaining cards) from its undealt list and appending them to the
supplied player's hand. GameService persists the updated aggregate once through
GameRepository.update. Game.dealCards replaces the existing member entry with
the updated player instance returned by the ownership-checked lookup. This ensures the game
mapper persists the dealt hand as well as the remaining shoe, even when the
player lookup produces a separate domain instance. Both changes are saved in
the same transaction without a separate player write.
Positive-count validation belongs to the API request layer. Empty undealt lists return
an empty list. Undealt order must survive reloading; player hands must also be saved, and discarded cards never become available again.
Further service methods, HTTP error mapping, and concurrent mutation handling
remain future work.

### 6. Configure H2 in-memory persistence

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

### 7. Create controllers

Implemented as `GameController` and `DeckController`, including game-scoped
player routes, request/response DTOs, and `GameResponseMapper` for all responses returned by GameController and
`DeckResponseMapper` for responses returned by DeckController.
`ErrorResponseMapper` remains separate. The developer labels
this work implementation step 5. Request validation and centralized exception
handling are separate steps; no tests were added in the controller step.

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

- its undealt card list
- attached decks
- cards
- players
- hands

Unassigned decks remain outside the game.

When removing a player:

- remove the player from active game membership
- discard all cards in their hand
- do not return those cards to the undealt list
- discarded cards cannot be dealt again

A deck belongs to at most one game at a time.

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

Use the approved [shoe storage and shuffle design](domain.md#shoe-storage-counting-and-shuffling):
an ArrayList containing only undealt cards, with its last entry next to deal, and a nested
EnumMap<Suit, EnumMap<Rank, Integer>> for remaining-face counts.
Shoe delegates end-removal to CardDealer in O(cards dealt), then updates its
CardCounter. It delegates the count index to its own CardCounter,
and delegates domain shuffle to CardShuffler. CardCounter owns the nested EnumMap
and exposes card additions/removals and face/suit queries; each shoe receives a
fresh counter, rebuilt from the undealt list on restoration. The shuffler implements Fisher–Yates and uses a default RNG; its
RandomGenerator overload allows deterministic tests. Shoe provides default collaborators and a full injection constructor for mocks.
GameFactory and GameEntityMapper need no domain-service wiring; the mapper
constructs Shoe from persistent state. GameTest mocks Shoe, and ShoeTest mocks
its card collaborators.
GameService.shuffleCards loads the game, shuffles its shoe, and saves the game.
GameService.getGame returns the domain game; response mappers obtain counts from
its shoe; no separate service methods for those counts are needed. HTTP endpoints and
response mappers remain future work.

Implement in-place Fisher–Yates directly using a library random-number generator.
For each index i from the end of the undealt list backward while i > 0, select j uniformly
from 0 through i (inclusive), then swap those entries.
This produces a uniform random permutation with uniform bounded choices, runs in
O(n) time, and uses O(1) auxiliary space. It avoids random-priority sorting and
collision handling. Do not call a library-provided shuffle operation.
Shuffle returns void and leaves the count map unchanged.

Persist the complete undealt list in order and rebuild
the count map from that list on restoration. Keep original deck contents unchanged.
See the [NIST algorithm reference](https://xlinux.nist.gov/dads/HTML/fisherYatesShuffle.html)
for Fisher–Yates.

Shuffle only undealt cards and preserve:

- cards already held in hands
- discarded cards
- card counts
- suit/rank values and occurrence counts

### 9. Add centralized error handling

Implemented with ApiExceptionHandler (`@RestControllerAdvice`) and a separate
ErrorResponseMapper. Controllers do not catch exceptions. Responses use
`application/problem+json` and contain `detail`, `status`, and `code`.

- Missing games, decks, or players: 404 with GAME_NOT_FOUND, DECK_NOT_FOUND,
  or PLAYER_NOT_FOUND. A player outside the requested game also maps to 404.
- DeckAlreadyAttachedException: 422 with DECK_ALREADY_ASSIGNED. Deck throws this
  specific exception rather than a generic IllegalStateException.
- All other exceptions: 500 with INTERNAL_ERROR and a generic detail. The
  exception is logged server-side; its internal message is not sent to clients.

Request validation is implemented; additional HTTP error mappings remain future work.
The statuses above supersede older proposed mappings.

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
  "status": 422,
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
- `GameFactory` creates a game with empty deck, undealt-card, and player collections
- `DeckFactory` supplies identity only; `Deck.generateCards()` creates exactly 52 cards on attachment
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
- rehydration preserves stored game/player cards and deck identity/ownership
- rehydration preserves deck attachment
- rehydration preserves dealt/discarded state

#### Domain tests

Verify:

- 52 unique cards per deck
- Ace-to-King values
- hand totals
- player ordering
- deck attachment invariants
- Fisher–Yates preserves every card
- dealing removes cards only from the game's undealt list
- dealing preserves card values and their occurrence counts
- removing a player discards their hand
- discarded cards never return to the undealt list

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
- a deck cannot be attached to multiple games
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
- Removing a player discards their cards without returning them to the undealt list.
- A deck belongs to only one game at a time.
- Attached decks cannot be removed.
- Adding a deck returns `204 No Content`.
- `ErrorResponse` contains `detail`, `status`, and `code`.

## Remaining proposals

Canonical initial/appended card order remains a proposal.

The implemented error media type is `application/problem+json`.

### Deck persistence simplification (implemented)

GameEntity and Shoe no longer retain deck collections. DeckEntity stores only ID and its game association. DeckService persists attachment separately in the same
transaction as the game's undealt-card update. Deleting a game explicitly deletes
attached deck records before deleting the game and its players. Unattached decks
remain available. Only game undealt cards and player hands store card JSON.
