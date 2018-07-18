package code.network;

import code.gui.GameController;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class ChatClient extends SocketThread {
    private GameController gameController;
    private String name;
    private final List<String> messageQueue = new ArrayList<>();

    public ChatClient(String name, String ipAddress, int port, Runnable onFail, Runnable onSuccess, Runnable onDisconnect, GameController gameController) {
        super(ipAddress, port, onFail, onSuccess, onDisconnect);
        this.gameController = gameController;
        this.name = name;
    }

    public void sendMessage(String message) {
        synchronized (messageQueue) {
            messageQueue.add(name + ": " + message);
        }
    }

    @Override
    void afterConnection() throws IOException, InterruptedException {
        DataInputStream in = clientSockets.get(0).in;
        DataOutputStream out = clientSockets.get(0).out;
        Socket socket = clientSockets.get(0).socket;

        out.writeBoolean(false); //register as a chat socket

        socket.setSoTimeout(checkTimeout);
        while (!exit) {
            Thread.sleep(100);
            synchronized (messageQueue) {
                if (messageQueue.size() > 0) {
                    socket.setSoTimeout(communicationTimeout);
                    for (String message : messageQueue) {
                        out.writeUTF(message);
                    }
                    messageQueue.clear();
                    socket.setSoTimeout(checkTimeout);
                }
            }
            try {
                String newMessage = in.readUTF();
                gameController.addChatMessage(newMessage);
                if (Main.gameServer != null) {
                    Main.gameSetupController.addChatMessage(newMessage);
                }
            } catch (SocketTimeoutException e) {
            }
        }
    }
}
