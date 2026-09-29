# A Basic Deck of Cards Game

The game API is a very basic game in which one or more decks are added to create a **game deck**, commonly referred to as a **shoe**, along with a group of players getting cards from the game deck.

A deck is defined as follows:

- Fifty-two playing cards
- Four suits:
  - hearts
  - spades
  - clubs
  - diamonds
- Face values:
  - Ace
  - 2–10
  - Jack
  - Queen
  - King

## API Required Operations

- Create and delete a game
- Create a deck
- Add a deck to a game deck 
  - Once a deck has been added to a game deck it cannot be removed.
- Add and remove players from a game
- Deal cards to a player in a game from the game deck
  - For a game deck containing only one deck of cards, a call to shuffle followed by 52 calls to `dealCards(1)` for the same player should result in the caller being provided all 52 cards of the deck in a random order.
  - If the caller then makes a 53rd call to `dealCard(1)`, no card is dealt. 
  - This approach is to be followed if the game deck contains more than one deck.
- Get the list of cards for a player
- Get the list of players in a game with hand values
  - Return the list of players in a game along with the total added value of all the cards each player holds.
  - Use face values of cards only. 
  - Sort the list in descending order, from the player with the highest value hand to the player with the lowest value hand.
  - Example:
    - Player `A` holds a 10 + King → total value = 23
    - Player `B` holds a 7 + Queen → total value = 19
    - Player `A` is listed before player `B`
- Get the count of cards per suit remaining undealt
  - Example:
    - 5 hearts
    - 3 spades
    - etc.
- Get the count of each card remaining in the game deck
  - Return the count of each card by:
    - suit
    - value
  - Sort by suit in this order:
    1. hearts
    2. spades
    3. clubs
    4. diamonds
  - Within each suit, sort by face value from highest to lowest:
    1. King
    2. Queen
    3. Jack
    4. 10
    5. ...
    6. 2
    7. Ace
  - Ace has a value of `1`.
- Shuffle the game deck (shoe)
  - Shuffle returns no value, but results in the cards in the game deck being randomly permuted.
  - Do **not** use library-provided shuffle operations to implement this function.
  - You may use library-provided random number generators.
  - Shuffle can be called at any time.
