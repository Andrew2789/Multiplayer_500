package Java.Logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Game {
    public List<Player> players = new ArrayList<>();
    public List<Card> kitty;
    public List<Card> trick;
    public List<Player> trickPlayers;
    public List<Integer> tricksWon;
    public Bid bid;

    private List<Card> createDeck() {
        List<Card> deck = new ArrayList<>();
        char[] suits = {'s', 'c', 'd', 'h'};
        deck.add(new Card('c', 4));
        deck.add(new Card('s', 4));
        for (char suit: suits) {
            for (int val = 5; val < 14; val++) {
                deck.add(new Card(suit, val));
            }
        }
        for (char suit: suits) {
            deck.add(new Card(suit, 1));
        }
        deck.add(new Card('j', 0));
        return deck;
    }

    public void deal() {
        List<Card> deck = createDeck();
        Collections.shuffle(deck);

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 10; j++) {
                players.get(i).hand.add(deck.remove(0));
            }
        }
        kitty = deck;
        tricksWon = new ArrayList<>(Arrays.asList(0, 0));
    }

    public Player findWinner(char leadingSuit) {
        List<Integer> scores = new ArrayList<>();
        for (Card card: trick) {
            scores.add(bid.valueOf(card, leadingSuit));
        }
        int maxIndex = scores.indexOf(Collections.max(scores));
        Player winningPlayer = trickPlayers.get(maxIndex);
        int winnerIndex = players.indexOf(winningPlayer);
        tricksWon.set(winnerIndex % 2, tricksWon.get(winnerIndex % 2) + 1);
        return winningPlayer;
    }
}
