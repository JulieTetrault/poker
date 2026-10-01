# Domain UML


```mermaid
classDiagram
    direction LR

    class Suit {
        <<enumeration>>
        HEARTS
        SPADES
        CLUBS
        DIAMONDS
        -String label
        +getLabel() String
    }

    class Rank {
        <<enumeration>>
        ACE
        TWO
        THREE
        FOUR
        FIVE
        SIX
        SEVEN
        EIGHT
        NINE
        TEN
        JACK
        QUEEN
        KING
        -int value
        -String label
        +getValue() int
        +getLabel() String
    }

    class Game {
        <<aggregate root>>
        -UUID id
        -String name
        -Shoe shoe
        -Map~UUID, Player~ players
        +getPlayers() List~Player~
        +addPlayer(Player player) void
        +removePlayer(Player player) void
        +addDeck(Deck deck) void
        +dealCards(int cardCount, Player player) List~Card~
    }

    class Shoe {
        <<domain model>>
        -List~Card~ cards
        -CardShuffler cardShuffler
        -CardCounter cardCounter
        -CardDealer cardDealer
        +getCards() List~Card~
        +addDeck(Deck deck) void
        +dealCards(int cardCount) List~Card~
        +getUndealtCardCounts() FaceCounts
        +getUndealtSuitCardsCount() Map~Suit, Integer~
        +shuffle() void
    }

    class CardDealer {
        <<domain service>>
        +dealCards(List~Card~ cards, int cardCount) List~Card~
    }

    class CardCounter {
        <<domain service>>
        -EnumMap cardCounts
        +addCards(List~Card~ cards) void
        +removeCards(List~Card~ cards) void
        +getCardsCount() FaceCounts
        +getSuitCardsCount() Map~Suit, Integer~
    }

    class CardShuffler {
        <<domain service>>
        +shuffle(List~Card~ cards) void
    }

    class Deck {
        <<aggregate root>>
        -UUID id
        -UUID gameId
        +generateCards() List~Card~
        +setGameId(UUID gameId) void
    }

    class Card {
        <<value object>>
        -Suit suit
        -Rank rank
    }

    class Player {
        <<aggregate root>>
        -UUID id
        -UUID gameId
        -String name
        -List~Card~ cards
        +getCards() List~Card~
        +addCards(List~Card~ cards) void
        +getHandValue() int
    }

    Game "1" *-- "1" Shoe : shoe
    Game "1" o-- "0..*" Player : players
    Shoe "1" o-- "0..*" Card : undealt cards
    Shoe --> CardDealer : delegates
    Shoe "1" *-- "1" CardCounter : counts
    Shoe --> CardShuffler : delegates
    Deck ..> Card : generates 52
    Deck --> Game : gameId
    Player --> Game : gameId
    Player "1" o-- "0..*" Card : dealt cards
    Card --> Suit : suit
    Card --> Rank : rank
```

## Domain Notes

`Shoe` stores the game's state. It three domain services: `CardDealer` removes cards,
`CardCounter` tracks suit/rank counts, and `CardShuffler` changes card order.
This separates responsibilities and makes each behavior independently testable.


## Shuffle

`CardShuffler` uses in-place Fisher–Yates: walk backward from the last card,
choose a random index from zero through the current index, and swap those cards.
All undealt cards are shuffled together; hands and counts stay unchanged.
Uniform random choices make every permutation equally likely. The algorithm
takes O(n) time and O(1) extra space.
