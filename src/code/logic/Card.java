package code.logic;

public class Card {
    private char suit;
    private int val;

    public Card(char suit, int val) {
        this.suit = suit;
        this.val = val;
    }

    public Card(String card) {
        if (card == null || card.length() < 2) {
            throw new IllegalArgumentException();
        }
        this.suit = card.charAt(0);
        this.val = Integer.parseInt(card.substring(1));
    }

    public char getSuit() {
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
            case 's':
                hashCode += 1 << 4;
                break;
            case 'c':
                hashCode += 2 << 4;
                break;
            case 'd':
                hashCode += 3 << 4;
                break;
            case 'h':
                hashCode += 4 << 4;
                break;
        }
        return hashCode;
    }

    public String toString() {
        return Character.toString(suit) + val;
    }
}
