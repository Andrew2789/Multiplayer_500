package Java.Logic;

public class Bid {
    private int tricks;
    private char suit;

    public Bid(int tricks, char suit) {
        this.tricks = tricks;
        this.suit = suit;
    }

    public int getPoints() {
        switch (suit) {
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
            case 'm': //miseer, tricks 0 = normal miseer, tricks 1 = open miseer
                if (tricks == 0) {
                    return 250;
                } else {
                    return 400;
                }
                default:
                    throw new IllegalStateException("This bid has an invalid suit or trick number");
        }
    }

    public int valueOf(Card card, char leadingSuit) {
        if (card.getSuit() == 'j') {
            return 40;
        }
        int value = card.getVal();
        if (value == 1) { //Ace value is adjusted to 14, 1 higher than king
            value = 14;
        } else if (value == 11 && (suit != 'n' && suit != 'm')) { //Check for bowers
            if (suit == card.getSuit()) {
                value = 16; //right bower
            } else if (suit == 's' && card.getSuit() == 'c' ||
                suit == 'c' && card.getSuit() == 's' ||
                suit == 'd' && card.getSuit() == 'h' ||
                suit == 'h' && card.getSuit() == 'd') {
                value = 35; //value = 15 for left bower, +20 for being trump suit
            }
        }

        if (card.getSuit() != suit && card.getSuit() != leadingSuit && value < 20) {
            return 0;
        }

        if (card.getSuit() == suit) { //Card is trump suit
            value += 20;
        }
        return value;
    }
}
