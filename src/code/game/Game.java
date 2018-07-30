package code.game;

import java.util.*;

public class Game {
    private List<Player> players;
    private GameState gameState;

    private Bid bid;
    private int bidWinner;

    private List<Card> kitty = new ArrayList<>();
    private List<Card> trick = new ArrayList<>();
    private List<Player> trickPlayers = new ArrayList<>();
    private List<Integer> tricksWon = new ArrayList<>();
    private int roundNumber;
    private Map<BidType, Boolean> suitsPlayed;

    public Game(List<Player> players) {
        this.players = players;
        this.gameState = GameState.SETUP;
    }

    public void setBid(Bid bid, int startingPlayer) {
        this.bid = bid;
        this.bidWinner = startingPlayer;
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
        roundNumber = 0;
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
        hostDealt();
    }

    public void hostDealt() {
        tricksWon = new ArrayList<>(Arrays.asList(0, 0));
        suitsPlayed = new HashMap<>();
        for (BidType suit: BidType.getTrumpSuits()) {
            suitsPlayed.put(suit, false);
        }
        roundNumber++;
    }

    public void cardPlayed(Card card, int playerIndex, boolean playedBySelf) {
        if (playedBySelf) {
            trick.add(getPlayer(playerIndex).playCard(card));
        } else {
            trick.add(card);
        }
        trickPlayers.add(getPlayer(playerIndex));
        suitsPlayed.put(bid.getType().getSuitInBid(card), true);
    }

    public Player findTrickWinner() {
        List<Integer> scores = new ArrayList<>();
        for (Card card: trick) {
            scores.add(bid.getType().valueOf(card, getLeadingSuit()));
        }
        int maxIndex = scores.indexOf(Collections.max(scores));
        Player winningPlayer = trickPlayers.get(maxIndex);
        int winnerIndex = players.indexOf(winningPlayer);
        tricksWon.set(winnerIndex % 2, tricksWon.get(winnerIndex % 2) + 1);
        trick.clear();
        trickPlayers.clear();
        return winningPlayer;
    }

    public boolean wasBidSuccessful() {
        boolean won = tricksWon.get(bidWinner % 2) >= bid.getTricks();
        players.get(0).updatePoints(won, bid);
        players.get(1).updatePoints(getTrickPoints(), bid);
        players.get(2).updatePoints(won, bid);
        players.get(3).updatePoints(getTrickPoints(), bid);

        return won;
    }

    public int getTrickPoints() {
        return tricksWon.get((bidWinner + 1) % 2)*10;
    }

    private List<Card> createDeck() {
        List<Card> deck = new ArrayList<>();
        BidType[] suits = BidType.getTrumpSuits();
        deck.add(new Card(BidType.CLUBS, 4));
        deck.add(new Card(BidType.SPADES, 4));
        for (BidType suit: suits) {
            for (int val = 5; val < 14; val++) {
                deck.add(new Card(suit, val));
            }
        }
        for (BidType suit: suits) {
            deck.add(new Card(suit, 1));
        }
        deck.add(new Card(BidType.NO_TRUMPS, 0));
        return deck;
    }

    public BidType getLeadingSuit() {
        if (trick.size() == 0) {
            return null;
        } else if (bid.getType().isLeftBower(trick.get(0))) {
            return trick.get(0).getSuit().getOffsuit();
        } else if (trick.get(0).getSuit() == BidType.NO_TRUMPS) {
            if (bid.getType() == BidType.MISERE || bid.getType() == BidType.NO_TRUMPS) {
                //scream TODO fix this
                return BidType.CLUBS;
            } else {
                return bid.getType();
            }
        } else {
            return trick.get(0).getSuit();
        }
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public List<Card> getTrick() {
        return Collections.unmodifiableList(new ArrayList<>(trick));
    }

    public List<Integer> getTricksWon() {
        return Collections.unmodifiableList(tricksWon);
    }

    public List<Player> getTrickPlayers() {
        return Collections.unmodifiableList(new ArrayList<>(trickPlayers));
    }

    public Player getPlayer(int index) {
        return players.get(index);
    }

    public boolean getTrumpsPlayed() {
        return suitsPlayed.get(bid.getType());
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public int getBidWinner() {
        return bidWinner;
    }

    public Bid getBid() {
        return bid;
    }
}
