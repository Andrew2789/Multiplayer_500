package Java.Logic;

import java.util.List;

public class Player {
    private String name;
    public List<Card> hand;

    public Player(String name) {
        this.name = name;
    }

    public Player(String name, List<Card> hand) {
        this.name = name;
        this.hand = hand;
    }

    public String getName() {
        return name;
    }
}
