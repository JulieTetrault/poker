# API implementation plan

Status: implementation steps with confirmed API decisions, September 29, 2026.
Game endpoints are not yet
implemented. Follow the [requirements](requirements.md), [domain model](domain.md),
and [OpenAPI contract](openapi.json). Use the [manual testing guide](manual-api-testing.md)
when the API is available.

## Implementation steps

### 1. Create domain classes

Create `Game`, `Shoe`, `Deck`, `Card`, `Player`, and `Hand`, plus `Suit` and
`Rank` enums, following the domain model:

- A game owns one shoe and its players; each player owns one hand.
- A shoe contains decks and manages undealt cards. A new deck has 52 cards and
  a null `shoeId` until attached. An attached deck cannot be removed.
- Give cards their own UUID and original `deckId`. Derive their value from
  `Rank.getValue()`: Ace = 1 through King = 13.
- Keep dealing, remaining-card counts, and shuffling in `Shoe`; hand totals in
  `Hand`; player ordering in `Game`. Keep domain classes independent of Spring.
- Track undealt cards separately from each deck's original 52-card collection
  so dealing does not change a card's original deck identity.

### 2. Create DTOs for request and response payloads

Create separate web DTOs matching the OpenAPI schemas; do not expose mutable
domain collections directly. Map domain objects to response snapshots.

Request DTOs are `CreateGameRequest` (required name), `AddDeckRequest` (deck ID),
`AddPlayerRequest` (name), and `DealCardsRequest` (positive count).

Response DTOs are `CreateGameResponse`, `CreateDeckResponse`,
`AddPlayerResponse`, `DealCardsResponse`, `GetPlayerCardsResponse`,
`GetPlayersHandValueResponse`, `GetUndealtCardsResponse`,
`GetUndealtSuitCardsCountResponse`, and `ErrorResponse`. Validate required fields, UUIDs,
nonblank names of at most 100 characters, and positive integer deal counts.
Reject unknown fields and incorrect JSON types rather than coercing them.

### 3. Create controllers

Create game, deck, and player controllers using the routes below. Controllers
handle HTTP input/output and delegate operations to services. Wire business
behavior in step 5. Use the methods and statuses currently declared in OpenAPI.

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

### 4. Create storage

Provide a small storage abstraction for games and decks. In-memory storage is a
proposed starting point; restart would clear state. Add persistence only if a
requirement calls for it. Keep games isolated and deck ownership consistent.

### 5. Create services and connect the controllers

Services resolve resources, check business constraints, invoke domain behavior,
and map results to DTOs. Resolve the game before its nested player; a player from
another game is not found in the requested game. Rejected mutations leave state
unchanged. Make attachment and card transfers atomic so concurrent requests cannot
assign the same deck twice or deal the same physical card twice.

Implement game/deck creation and deletion, deck attachment, player membership,
dealing, hand retrieval, player ordering, both undealt counts, and shuffle.
Return players by total face value descending, then player UUID ascending.
Delete everything owned by a deleted game, including its players. Removed
players' cards remain dealt/discarded and cannot be dealt again. A deck belongs
to at most one shoe at a time; each shoe belongs to one game.

Deal `min(requested count, available cards)` and return `DealCardsResponse`
with numeric `dealtCards` and `remainingCards`. Requesting 13 with 10 remaining
returns `{"dealtCards":10,"remainingCards":0}`; an empty shoe returns both zero.
Adding a deck returns 204 without a body; adding a player returns 201 with
`AddPlayerResponse`.

Count only undealt cards, ordered
hearts, spades, clubs, diamonds and King down to Ace within each suit.

Implement Fisher–Yates directly using a random number generator; do not call a
library shuffle operation. Shuffle only undealt cards and preserve hands and
counts. An empty shoe deals no cards, including the required 53rd one-card deal
from a single deck.

### 6. Add centralized error handling

Use a REST exception handler and the `ErrorResponse` DTO: `message`, `status`,
and stable `code`. Preserve the catalogue below. Do not expose stack traces or
internal exception messages. Clients should use status and code rather than
parse the message. The current contract declares `application/problem+json`;
its simplified schema differs from its older Problem Details descriptions.
Resolve that media-type/format decision before implementation.

| HTTP | Code | Message / handling |
| --- | --- | --- |
| 400 | `INVALID_REQUEST` | `Request body must be valid JSON.` Missing required body: `Request body is required.` Unsupported body: `This operation does not accept a request body.` |
| 400 | `INVALID_PARAMETER` | `Parameter '{name}' must be a UUID.` |
| 400 | `VALIDATION_FAILED` | `One or more request fields are invalid.` |
| 404 | `GAME_NOT_FOUND` | `Game '{gameId}' was not found.` |
| 404 | `DECK_NOT_FOUND` | `Deck '{deckId}' was not found.` |
| 404 | `PLAYER_NOT_FOUND` | `Player '{playerId}' was not found in game '{gameId}'.` |
| 404 | `ENDPOINT_NOT_FOUND` | `The requested endpoint was not found.` |
| 405 | `METHOD_NOT_ALLOWED` | `The HTTP method is not supported for this endpoint.` Include the correct `Allow` header. |
| 406 | `NOT_ACCEPTABLE` | `The requested response media type is not supported.` Body may be empty if the client also rejects the error media type. |
| 409 | `DECK_ALREADY_ASSIGNED` | `Deck '{deckId}' is already assigned to a game.` |
| 415 | `UNSUPPORTED_MEDIA_TYPE` | `Content-Type must be application/json.` Applies to operations accepting JSON bodies. |
| 500 | `INTERNAL_ERROR` | `An unexpected error occurred.` Log the underlying error on the server. |

### 7. Test and verify

Use JUnit Jupiter and AssertJ for domain/service tests, Mockito when needed,
and MVC tests for routing, payloads, validation, and errors. Use full Spring
contexts only for behavior that requires them.

Verify 52 unique cards per deck, Ace-to-King values, independent games, permanent
attachment, deletion of game-owned resources, discarded removed hands, player
membership, hand totals and UUID tie ordering, and both undealt counts.
Verify a request for 13 cards with 10 remaining deals 10 and reports zero remaining;
empty-shoe requests report zero dealt and zero remaining.
After shuffle, 52 one-card deals must return all cards once; the 53rd deals none.
Repeat with two decks and 104/105 deals. Check that shuffle preserves cards and
hands, and concurrent mutations do not duplicate or lose cards. Use controlled
random draws for shuffle tests instead of requiring every shuffle to change order.

After Java changes, select the pinned JDK with `sdk env`, run
`./mvnw spotless:apply`, `./mvnw validate`, and `./mvnw verify`. Keep OpenAPI and
manual examples aligned with implemented behavior, and record each development
step and actual checks in the development log. Mock examples do not verify game
behavior.

## Confirmed decisions

- Game creation requires a name and returns its ID and name.
- Deals return actual dealt and remaining counts; partial and empty deals succeed.
- Deleting a game deletes its shoe, assigned decks, cards, players, and hands.
  Players must be added again for a new game. Unassigned decks are outside the game.
- Removing a player discards their cards without returning them to the shoe.
- A deck can belong to only one game shoe at a time; a shoe belongs to one game.
  Attached decks cannot be removed, as specified in the requirements.
- Players are ordered by hand value descending, then UUID ascending.
- Adding a deck has no response body. Adding a player returns its ID and name;
  use 201 Created for this creation response.
- Endpoint payloads and examples use the existing request/response schemas.

## Remaining proposals

In-memory storage and canonical initial/appended card order remain proposals.
The error schema contains `message`, `status`, and `code`, while the contract
still declares `application/problem+json`; settle the media type before
implementation. None of these choices adds a business requirement.
