package code.game;

import java.util.ArrayList;
import java.util.List;

public enum BidType {
    SPADES('s', "♤", "spades"),
    CLUBS('c', "♧", "clubs"),
    DIAMONDS('d', "♢", "diamonds"),
    HEARTS('h', "♡", "hearts"),
    NO_TRUMPS('n', "\uD83D\uDEAB", "no trumps"),
    MISERE('m', "M", "misere");

    private char bidChar;
    private String symbol, word;

    BidType(char bidChar, String symbol, String word) {
        this.bidChar = bidChar;
        this.symbol = symbol;
        this.word = word;
    }

    public List<Card> filterPlayable(List<Card> cards, boolean trumpsPlayed, BidType leadingSuit) {
        List<Card> playableCards = new ArrayList<>();
        if (leadingSuit == null && trumpsPlayed) {
            playableCards.addAll(cards);
            return playableCards;
        }
        for (Card card: cards) {
            BidType cardSuit = card.getSuit();
            if (isLeftBower(card) || cardSuit == NO_TRUMPS) {
                cardSuit = this;
            }
            if (cardSuit == NO_TRUMPS || cardSuit == MISERE) { //if a joker in a no trumps or misere bid
                playableCards.add(card);
            } else if (leadingSuit == null) {
                if (cardSuit != this) {
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

    public int valueOf(Card card, BidType leadingSuit) {
        if (card.getSuit() == BidType.NO_TRUMPS) { //joker
            return 40;
        }
        int value = card.getVal();
        if (value == 1) { //Ace value is adjusted to 14, 1 higher than king
            value = 14;
        } else if (card.getVal() == 11 && (this != BidType.NO_TRUMPS && this != BidType.MISERE)) { //Check for bowers
            if (this == card.getSuit()) {
                value = 16; //right bower
            } else if (isLeftBower(card)) {
                value = 35; //value = 15 for left bower, +20 for being trump trumpSuit
            }
        }

        if (card.getSuit() != this && card.getSuit() != leadingSuit && value < 20) {
            return 0;
        }

        if (card.getSuit() == this) { //Card is trump trumpSuit
            value += 20;
        }
        return value;
    }

    public boolean isLeftBower(Card card) {
        return card.getVal() == 11 && (this.getOffsuit() == card.getSuit());
    }

    public static BidType[] getTrumpSuits() {
        return new BidType[]{SPADES, CLUBS, DIAMONDS, HEARTS};
    }

    public static BidType[] getSuits() {
        return new BidType[]{SPADES, CLUBS, DIAMONDS, HEARTS, NO_TRUMPS};
    }

    public BidType getOffsuit() {
        switch (this) {
            case SPADES:
                return CLUBS;
            case CLUBS:
                return SPADES;
            case DIAMONDS:
                return HEARTS;
            case HEARTS:
                return DIAMONDS;
                default:
                    throw new IllegalStateException(word + "has no offsuit.");
        }
    }

    public char getChar() {
        return bidChar;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getWord() {
        return word;
    }

    public static BidType fromChar(char bidChar) {
        for (BidType bidType : BidType.values()) {
            if (bidType.bidChar == bidChar) {
                return bidType;
            }
        }
        return null;
    }
}
