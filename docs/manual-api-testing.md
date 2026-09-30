# API documentation and manual testing

## What works during planning

[The OpenAPI draft](openapi.json) documents all 12 planned operations, schemas,
request bodies, success examples and named error examples. The
[implementation plan](implementation-plan.md) explains proposals and acceptance
criteria. No game endpoint exists yet.

From the repository root, serve the documentation using Python 3:

```sh
python3 -m http.server 8000 --bind 127.0.0.1 --directory docs
```

Open <http://localhost:8000/index.html>. The viewer loads pinned Swagger UI
5.33.0 assets from jsDelivr, so internet access is required for those assets.
Expand an operation to inspect its request and all response examples. An optional
alternative is to import `docs/openapi.json` into a Swagger Editor supporting
OpenAPI 3.1. [Swagger Editor documentation](https://swagger.io/docs/open-source-tools/swagger-editor/).

To send requests before controllers exist, run the optional stateless mock in a
second terminal (requires Node.js 24.18.0 or newer and npm; first run downloads the pinned CLI):

```sh
npx --yes @stoplight/prism-cli@5.16.0 mock docs/openapi.json --host 127.0.0.1 --port 4010
```

If your active Node is older, this alternative runs a pinned temporary runtime
without changing your installed Node version:

```sh
npm exec --yes --cache /tmp/poker-api-npm-cache --package node@24.18.0 --package @stoplight/prism-cli@5.16.0 -- prism mock docs/openapi.json --host 127.0.0.1 --port 4010
```

Select the **Stateless contract mock** server in Swagger UI and use **Try it out**.
For example, execute `POST /api/v1/games`, then use the example IDs in the other
operations. Mock IDs are examples, not newly persisted resources. Prism enables
CORS for local browser access by default.

To inspect a named planned failure directly:

```sh
curl -i -X POST http://localhost:4010/api/v1/games/11111111-1111-4111-8111-111111111111/decks \
  -H 'Content-Type: application/json' \
  -H 'Prefer: code=422, example=DECK_ALREADY_ASSIGNED' \
  -d '{"deckId":"22222222-2222-4222-8222-222222222222"}'
```

Prism generates responses from the contract; it does not simulate game state,
shuffle, decrement cards, or enforce deck ownership. Its request-validation
errors are tooling errors and may differ from the API's planned error format.
A mock success only verifies that a request and example can be exercised.
See the [Prism project and documentation](https://github.com/stoplightio/prism).

## Testing the actual implementation later

Run `sdk env` and `./mvnw spring-boot:run`, then use the planned same-origin Spring
Swagger UI integration. Until that integration exists, use curl/imported OpenAPI
in an HTTP client. Switching the separate viewer to port 8080 requires the API
to allow that local origin via development-only CORS; no CORS configuration has
been added during planning. A connection failure or current 404 from the
boilerplate is not evidence of planned business error handling.

Use IDs returned by the real API and keep a fresh game per scenario. Test both
HTTP metadata and body: status, content type, stable error code, detail, field
errors, `Allow` for 405, and absence of a body for 204. Rejected mutations must
leave state unchanged.

| Scenario | Actions | Expected result once implemented |
| --- | --- | --- |
| Empty game | Create game; read players and both shoe counts | 201; no players, four zero suit rows, 52 zero face rows |
| Deck basics | Create and attach deck | 201 each; 52 remaining, 13 per suit, one of every face |
| Assignment conflicts | Attach same deck twice; attach it to another game | 422 `DECK_ALREADY_ASSIGNED`; both games unchanged by failure |
| Player basics | Add A and B, read hands | 201 with distinct IDs; empty hands, zero totals |
| Single-deck exhaustion | Shuffle; deal count 1 to A 52 times; read hand; deal once more | 52 distinct physical cards/faces, total hand value 364; final 200 with zero dealt |
| Multi-deck exhaustion | Fresh game with two distinct decks; 104 one-card deals, then one extra | Each face twice with distinct deck IDs; 105th returns no cards |
| Partial deal proposal | Fresh single deck; deal 50, then request 5 | 50 then 2 dealt; zero remaining; later request returns zero |
| Ranking | Deal to multiple players; sum the actual cards returned | Totals equal face sums; descending totals, UUID order for ties |
| Count order | Inspect suit and face count responses after deals | Suit order hearts/spades/clubs/diamonds; King-to-Ace within suit; zeros included |
| Shuffle mid-game | Save hands and counts, shuffle, read them again, then exhaust shoe | 204; hands/counts unchanged; no previously dealt physical card repeats |
| Empty shuffle | Shuffle a new game or exhausted shoe | 204, no body |
| Remove player proposal | Remove player with cards; inspect shoe and other hands | 204; cards not returned; subsequent lookup/removal is 404 |
| Late deck addition | Deal some cards, then attach a fresh deck | Remaining increases by exactly 52; existing hands preserved |
| Delete game | Delete and call nested reads/mutations; delete again | 204 then 404 `GAME_NOT_FOUND` |
| Validation | Missing/null fields, blank/long name, bad UUID, unknown field, count 0/negative/fraction/string/overflow, malformed JSON | 400 with the applicable documented code; no mutation |
| Resource scope | Valid absent game/deck/player UUIDs; player from a different game | Correct 404 code; game resolved before nested resources |
| Protocol errors | Wrong method; text/plain on JSON-body route; unsupported Accept; unknown route; unexpected body | 405 with Allow; 415; 406; 404; 400 respectively |

The exact ranking example 23 versus 19, scripted shuffle swaps, race conditions,
and controlled 500 failures belong in automated tests with deterministic
fixtures. Manual calls cannot request particular cards, so do not add cheat
endpoints or assume the mock proves those behaviors.
