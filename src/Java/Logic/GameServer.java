package Java.Logic;

import java.io.IOException;

public class GameServer extends SocketThread {
    public GameServer(int port, int players, Runnable onFail, Runnable onSuccess, Runnable onDisconnect) {
        super(port, players, onFail, onSuccess, onDisconnect);
    }

    @Override
    void afterConnection() throws IOException, InterruptedException {

    }
}
