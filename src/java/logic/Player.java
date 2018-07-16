package java.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Player {
    private String name;
    private List<Card> hand;
    private int points;

    public Player(String name) {
        this.name = name;
        hand = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Card> getHand() {
        return Collections.unmodifiableList(hand);
    }

    public int getPoints() {
        return points;
    }

    public void resetPoints() {
        points = 0;
    }

    public void updatePoints(boolean won, Bid bid) {
        points += won ? bid.getPoints() : -bid.getPoints();
    }

    public Card playCard(Card card) {
        hand.remove(card);
        return card;
    }

    public void setHand(List<Card> hand) {
        this.hand = hand;
    }

    public String toString() {
        return name;
    }
}
