package Java.Logic;

import Java.GUI.GameController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameClient extends SocketThread {
    private Player self;
    private GameController gameController;
    private List<Card> trick = new ArrayList<>();

    public GameClient(String ipAddress, int port, Runnable onFail, Runnable onSuccess, Runnable onDisconnect, GameController gameController) {
        super(ipAddress, port, onFail, onSuccess, onDisconnect);
        this.gameController = gameController;
        self = new Player("test");
        self.hand.add(new Card("d1"));
        self.hand.add(new Card("d13"));
        self.hand.add(new Card("s11"));
        self.hand.add(new Card("c12"));
        self.hand.add(new Card("j0"));
        self.hand.add(new Card("h6"));
        gameController.updateHand(self.hand);
    }

    public void cardPlayed(Card card) {
        if (trick.size() < 4) {
            if (!self.hand.remove(card)) {
                //invalid play
            }

            trick.add(card);
            gameController.updateTrick(trick);
            gameController.updateHand(self.hand);
        }
    }

    @Override
    void afterConnection() throws IOException, InterruptedException {

    }
}
