# Implementation plan

Follow the [requirements](requirements.md), [domain model](domain.md), and
[OpenAPI contract](openapi.json).

## Architecture

Separate HTTP concerns, application workflows, domain behavior, and persistence.
Controllers translate requests; application services coordinate operations;
domain models enforce game behavior; repositories load and save state.

```text
Request → Controller → Application service → Domain model
                              ↕
                    Repository interface
                              ↕
                    Entity mapper → JPA/Hibernate → H2

Domain model → Response mapper → Response
```

Domain models and card services remain independent of Spring, JPA, and Jackson.
Repository interfaces belong to the domain; their implementations belong to
persistence. Separate entity and response mappers keep storage and API formats
from shaping the domain.

## 1. Implement domain models, factories, and domain services

Model `Game`, `Deck`, `Player`, `Shoe`, and immutable `Card` values with `Suit`
and `Rank`. Factories create games, decks, and players using an injected
`IdGenerator`, keeping identity generation outside model constructors.

Shoe owns the ordered undealt cards and delegates dealing, counting, and
shuffling to three domain services. Each shoe owns its counter. Use
Fisher–Yates to shuffle all remaining cards without a library shuffle.
Player derives hand value from its cards; a deck can be attached only once.

## 2. Implement JPA/Hibernate entities and entity mappers

Persist games, players, and deck ownership in separate entities. Entity mappers
convert between persistence and domain objects, restoring existing identities
and state without creation factories.

Store undealt cards and player hands as ordered JSON arrays. Preserve duplicate
card occurrences and restore Shoe counts from the saved list; Shoe has no
separate table, and cards have no individual rows.

## 3. Implement repositories

Define domain repository interfaces for games, decks, and players. Implement
them using Spring Data JPA and entity mappers so callers work with domain objects.
Missing resources raise domain exceptions. Player lookups verify game ownership;
game deletion also removes its players and attached decks.

## 4. Implement services

Use `GameService`, `DeckService`, and `PlayerService` to coordinate factories,
repositories, and domain operations. Place transaction boundaries around
workflows so deck attachment and card dealing save related changes together.

Keep card rules in the domain. Services load the required resources, invoke
behavior, persist changes, and order player listings by descending hand value.

## 5. Implement controllers, requests/responses, and response mappers

Implement every OpenAPI route in `GameController` and `DeckController`.
Controllers handle HTTP input and status codes, delegate to application services,
and return explicit response objects.

Group success mapping in `GameResponseMapper` and `DeckResponseMapper`.
Keep request and response objects separate from domain and persistence models
so the API contract can evolve independently.

## 6. Implement exception handlers

Centralize HTTP error handling in controller advice with a separate
`ErrorResponseMapper`. Return `detail`, `status`, and `code`: missing resources
map to 404, an already attached deck to 422, and unexpected errors to 500.
Log unexpected failures and return a generic public message.

## 7. Implement request validation

Validate nonblank names, UUID identifiers, and positive integer card counts
before invoking services. Use Jakarta constraints on request records with
`@Valid`, native UUID path binding, and custom body UUID validation.

Reject invalid JSON field types through strict binding. Map request validation
and binding failures to 400 `BAD_REQUEST`, with messages identifying the invalid
field. Keep validation at the API boundary.
