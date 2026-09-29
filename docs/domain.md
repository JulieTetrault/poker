# Domain UML

```mermaid
classDiagram
    direction LR

    class Suit {
        <<enumeration>>
        hearts
        spades
        clubs
        diamonds
    }

    class Rank {
        <<enumeration>>
        Ace
        2
        3
        4
        5
        6
        7
        8
        9
        10
        Jack
        Queen
        King

        +int getValue()
    }

    class Game {
        +UUID id
        +String name
        +Shoe shoe
        +Player[] players

        +void addDeck(Deck deck)
        +void addPlayer(Player player)
        +void removePlayer(UUID playerId)
        +Player[] getPlayersByHandValue()
    }

    class Shoe {
        +UUID id
        +UUID gameId
        +Deck[] decks

        +void addDeck(Deck deck)
        +void shuffle()
        +Card[] deal(int count)
        +Map~Suit, int~ countRemainingBySuit()
        +int getRemainingCardCount()
    }

    class Deck {
        +UUID id
        +UUID shoeId
        +Card[] cards
    }

    class Card {
        +UUID id
        +UUID deckId
        +Suit suit
        +Rank rank
    }

    class Player {
        +UUID id
        +UUID gameId
        +String name
        +Hand hand

        +void receiveCards(Card[] cards)
        +int getHandValue()
    }

    class Hand {
        +Card[] cards

        +void addCards(Card[] cards)
        +int getValue()
    }

    Game "1" *-- "1" Shoe : shoe
    Game "1" *-- "0..*" Player : players

    Shoe "1" o-- "0..*" Deck : decks
    Deck "1" *-- "52" Card : cards

    Player "1" *-- "1" Hand : hand
    Hand "1" o-- "0..*" Card : cards

    Shoe --> Game : gameId
    Deck --> Shoe : shoeId
    Card --> Deck : deckId
    Player --> Game : gameId

    Card --> Suit : suit
    Card --> Rank : rank
```

## Domain Notes

- `Game` owns exactly one `Shoe`.
- `Shoe` has its own `id` and references its owning game through `gameId`.
- `Deck` references the shoe it belongs to through `shoeId`.
- `Deck.gameId` is intentionally omitted because the game can be derived through `Deck.shoeId -> Shoe.gameId`; storing both would duplicate the relationship.
- A newly created deck may have a null `shoeId` until it is added to a game's shoe.
- `Card` has its own `id` and references its original deck through `deckId`.
- `Player` references its game through `gameId`.
- `Shoe` owns the domain behavior related to undealt cards: adding decks, shuffling, dealing, and remaining-card counts.
- `Card` does not store a numeric value; the value is derived from `Rank`.
- `Rank.getValue()` maps Ace to 1 through King to 13.
- `Player` owns one `Hand`.
- `Hand` aggregates dealt cards and owns hand-specific rules and calculations.
- Request/response DTOs are intentionally excluded.
