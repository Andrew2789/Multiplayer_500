package code.game;

public class Bid {
    private int tricks;
    private BidType bidType;

    public Bid(int tricks, BidType bidType) {
        this.tricks = tricks;
        this.bidType = bidType;
    }

    public Bid(String bid) {
        if (bid == null || bid.length() < 2) {
            throw new IllegalArgumentException();
        }
        this.bidType = BidType.fromChar(bid.charAt(0));
        this.tricks = Integer.parseInt(bid.substring(1));
    }

    public int getPoints() {
        switch (bidType) {
            case SPADES:
                return 40 + 100*(tricks - 6);
            case CLUBS:
                return 60 + 100*(tricks - 6);
            case DIAMONDS:
                return 80 + 100*(tricks - 6);
            case HEARTS:
                return 100 + 100*(tricks - 6);
            case NO_TRUMPS:
                return 120 + 100*(tricks - 6);
            case MISERE: //misere, tricks 0 = normal misere, tricks 1 = open misere
                if (tricks == 0) {
                    return 250;
                } else {
                    return 400;
                }
            default:
                throw new IllegalStateException("BidType not recognised.");
        }
    }

    public String toSymbolString() {
        if (bidType == BidType.MISERE) {
            switch (tricks) {
                case 0:
                    return "Misere";
                case 1:
                    return "OpenMsr";
                default:
                    throw new IllegalStateException("Invalid number of misere tricks.");
            }
        } else {
            return Integer.toString(tricks) + bidType.getSymbol();
        }
    }

    public int getTricks() {
        return tricks;
    }

    public BidType getType() {
        return bidType;
    }

    public String toString() {
        return Character.toString(bidType.getChar()) + tricks;
    }

    public String toWordString(boolean capitalised) {
        switch (bidType) {
            case SPADES:
            case CLUBS:
            case DIAMONDS:
            case HEARTS:
            case NO_TRUMPS:
                return String.format("%d %s", tricks, bidType.getWord());
            case MISERE:
                if (tricks == 0) {
                    return capitalised ? "Misere" : "misere";
                } else {
                    return capitalised ? "Open misere" : "open misere";
                }
                default:
                    throw new IllegalStateException("Invalid trump suit.");
        }
    }


}
