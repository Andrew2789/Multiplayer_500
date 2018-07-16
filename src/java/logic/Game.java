package java.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Game {
    private List<Player> players;
    private List<Card> kitty = new ArrayList<>();
    private List<Card> trick = new ArrayList<>();
    private List<Player> trickPlayers = new ArrayList<>();
    private List<Integer> tricksWon = new ArrayList<>();
    private Bid bid;

    public Game(List<Player> players) {
        this.players = players;
    }

    public void setBid(Bid bid) {
        this.bid = bid;
        trick.clear();
        trickPlayers.clear();
    }

    public void setTeams(List<Integer> teams) {
        if (teams == null || teams.size() != players.size()) {
            throw new IllegalArgumentException(String.format("Teams list must be the same length as the players list (%d).", players.size()));
        }
        List<Player> newPlayers = new ArrayList<>();
        newPlayers.add(players.get(teams.indexOf(1)));
        newPlayers.add(players.get(teams.indexOf(2)));
        newPlayers.add(players.get(teams.lastIndexOf(1)));
        newPlayers.add(players.get(teams.lastIndexOf(2)));
        players = newPlayers;
    }

    public void deal() {
        List<Card> deck = createDeck();
        Collections.shuffle(deck);

        for (int i = 0; i < 4; i++) {
            List<Card> hand = new ArrayList<>();
            for (int j = 0; j < 10; j++) {
                hand.add(deck.remove(0));
            }
            getPlayer(i).setHand(hand);
        }
        kitty = deck;
        tricksWon = new ArrayList<>(Arrays.asList(0, 0));
    }

    public void cardPlayed(Card card, int playerIndex, boolean playedBySelf) {
        if (playedBySelf) {
            trick.add(getPlayer(playerIndex).playCard(card));
        } else {
            trick.add(card);
        }
        trickPlayers.add(getPlayer(playerIndex));
    }

    public Player findTrickWinner() {
        List<Integer> scores = new ArrayList<>();
        for (Card card: trick) {
            scores.add(bid.valueOf(card, getLeadingSuit()));
        }
        int maxIndex = scores.indexOf(Collections.max(scores));
        Player winningPlayer = trickPlayers.get(maxIndex);
        int winnerIndex = players.indexOf(winningPlayer);
        tricksWon.set(winnerIndex % 2, tricksWon.get(winnerIndex % 2) + 1);
        trick.clear();
        trickPlayers.clear();
        return winningPlayer;
    }

    public int findRoundWinner() {
        return 0;
    }

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

    public char getLeadingSuit() {
        if (trick.size() == 0) {
            throw new IllegalArgumentException("No tricks have been played yet so there is no leading suit.");
        }
        return trick.get(0).getSuit();
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public List<Card> getTrick() {
        return Collections.unmodifiableList(trick);
    }

    public List<Integer> getTricksWon() {
        return Collections.unmodifiableList(tricksWon);
    }

    public Player getPlayer(int index) {
        return players.get(index);
    }

    public Bid getBid() {
        return bid;
    }
}
