package Java.Logic;

import java.io.IOException;

public class GameClient extends SocketThread {
    private Player self;

    public GameClient(String ipAddress, int port, Runnable onFail, Runnable onSuccess, Runnable onDisconnect) {
        super(ipAddress, port, onFail, onSuccess, onDisconnect);
    }

    public void cardPlayed(Card card) {
        System.out.println(card);
    }

    @Override
    void afterConnection() throws IOException, InterruptedException {

    }
}
