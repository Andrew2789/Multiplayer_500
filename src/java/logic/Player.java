package Java.Logic;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private String name;
    public List<Card> hand;
    public int points;

    public Player(String name) {
        this.name = name;
        hand = new ArrayList<>();
    }

    public Player(String name, List<Card> hand) {
        this.name = name;
        this.hand = hand;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }
}
