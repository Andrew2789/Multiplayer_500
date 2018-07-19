package code.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bid {
    private int tricks;
    private char trumpSuit;
    private Map<Character, String> suitSymbols;

    public Bid(int tricks, char trumpSuit) {
        this.tricks = tricks;
        this.trumpSuit = trumpSuit;
        initializeSuitSymbols();
    }

    public Bid(String bid) {
        if (bid == null || bid.length() < 2) {
            throw new IllegalArgumentException();
        }
        this.trumpSuit = bid.charAt(0);
        this.tricks = Integer.parseInt(bid.substring(1));
        initializeSuitSymbols();
    }

    private void initializeSuitSymbols() {
        suitSymbols = new HashMap<>();
        suitSymbols.put('s', "♠");
        suitSymbols.put('c', "♣");
        suitSymbols.put('d', "♦");
        suitSymbols.put('h', "♥");
        suitSymbols.put('n', "\uD83D\uDEAB");
    }

    public int getPoints() {
        switch (trumpSuit) {
            case 's':
                return 40 + 100*(tricks - 6);
            case 'c':
                return 60 + 100*(tricks - 6);
            case 'd':
                return 80 + 100*(tricks - 6);
            case 'h':
                return 100 + 100*(tricks - 6);
            case 'n':
                return 120 + 100*(tricks - 6);
            case 'm': //misere, tricks 0 = normal misere, tricks 1 = open misere
                if (tricks == 0) {
                    return 250;
                } else {
                    return 400;
                }
                default:
                    throw new IllegalStateException("This bid has an invalid trumpSuit or trick number");
        }
    }

    public int valueOf(Card card, char leadingSuit) {
        if (card.getSuit() == 'j') {
            return 40;
        }
        int value = card.getVal();
        if (value == 1) { //Ace value is adjusted to 14, 1 higher than king
            value = 14;
        } else if (card.getVal() == 11 && (trumpSuit != 'n' && trumpSuit != 'm')) { //Check for bowers
            if (trumpSuit == card.getSuit()) {
                value = 16; //right bower
            } else if (isLeftBower(card)) {
                value = 35; //value = 15 for left bower, +20 for being trump trumpSuit
            }
        }

        if (card.getSuit() != trumpSuit && card.getSuit() != leadingSuit && value < 20) {
            return 0;
        }

        if (card.getSuit() == trumpSuit) { //Card is trump trumpSuit
            value += 20;
        }
        return value;
    }

    public List<Card> filterPlayable(List<Card> cards, boolean trumpsPlayed, Character leadingSuit) {
        List<Card> playableCards = new ArrayList<>();
        if (leadingSuit == null && trumpsPlayed) {
            playableCards.addAll(cards);
            return playableCards;
        }
        for (Card card: cards) {
            char cardSuit = card.getSuit();
            if (isLeftBower(card) || cardSuit == 'j') {
                cardSuit = trumpSuit;
            }
            if (cardSuit == 'n' || cardSuit == 'm') { //if a joker in a no trumps or misere bid
                playableCards.add(card);
            } else if (leadingSuit == null) {
                if (cardSuit != trumpSuit) {
                    playableCards.add(card);
                }
            } else if (cardSuit == leadingSuit) {
                playableCards.add(card);
            }
        }

        if (playableCards.size() == 0) {
            playableCards.addAll(cards);
        }
        return playableCards;
    }

    public boolean isLeftBower(Card card) {
        return card.getVal() == 11 && (
            trumpSuit == 's' && card.getSuit() == 'c' ||
            trumpSuit == 'c' && card.getSuit() == 's' ||
            trumpSuit == 'd' && card.getSuit() == 'h' ||
            trumpSuit == 'h' && card.getSuit() == 'd');
    }

    public int getTricks() {
        return tricks;
    }

    public char getTrumpSuit() {
        return trumpSuit;
    }

    public String toString() {
        return Character.toString(trumpSuit) + tricks;
    }

    public String toWordString() {
        String suitName;
        switch (trumpSuit) {
            case 's':
                suitName = "spades";
                break;
            case 'c':
                suitName = "clubs";
                break;
            case 'd':
                suitName = "diamonds";
                break;
            case 'h':
                suitName = "hearts";
                break;
            case 'n':
                suitName = "no trumps";
                break;
            case 'm':
                if (tricks == 0) {
                    return "misere";
                } else {
                    return "open misere";
                }
                default:
                    throw new IllegalStateException("Invalid trump suit.");
        }
        return String.format("%d %s", tricks, suitName);
    }

    public String toSymbolString() {
        if (trumpSuit == 'm') {
            return toWordString();
        } else {
            return Integer.toString(tricks) + suitSymbols.get(trumpSuit);
        }
    }
}
