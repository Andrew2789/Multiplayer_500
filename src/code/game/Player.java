package code.game;

import code.game.Bid;
import code.game.Card;
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

    public void updatePoints(int points, Bid bid) {
        if (bid.getTrumpSuit() != 'm') {
            this.points += points;
        }
    }

    public Card playCard(Card card) {
        hand.remove(card);
        return card;
    }

    public void moveCard(int oldIndex, int newIndex) {
        if (oldIndex < newIndex) {
            newIndex--;
        }
        hand.add(newIndex, hand.remove(oldIndex));
    }

    public void setHand(List<Card> hand) {
        this.hand = hand;
    }

    public String toString() {
        return name;
    }
}
