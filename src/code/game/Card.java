package code.game;

public class Card {
    private BidType suit;
    private int val;

    public Card(BidType suit, int val) {
        this.suit = suit;
        this.val = val;
    }

    public Card(String card) {
        if (card == null || card.length() < 2) {
            throw new IllegalArgumentException();
        }
        this.suit = BidType.fromChar(card.charAt(0));
        this.val = Integer.parseInt(card.substring(1));
    }

    public BidType getSuit() {
        return suit;
    }

    public int getVal() {
        return val;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Card)) {
            return false;
        }
        Card other = (Card)obj;
        return (this.suit == other.suit) && (this.val == other.val);
    }

    @Override
    public int hashCode() {
        int hashCode = val;
        switch (suit) {
            case SPADES:
                hashCode += 1 << 4;
                break;
            case CLUBS:
                hashCode += 2 << 4;
                break;
            case DIAMONDS:
                hashCode += 3 << 4;
                break;
            case HEARTS:
                hashCode += 4 << 4;
                break;
        }
        return hashCode;
    }

    public String toString() {
        return Character.toString(suit.getChar()) + val;
    }
}
