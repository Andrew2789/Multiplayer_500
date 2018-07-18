package code.network;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class ChatServer extends SocketThread {
    public ChatServer(Runnable onDisconnect) {
        super(new ArrayList<>(), onDisconnect);
    }

    public void addClient(ClientSocket clientSocket) throws SocketException {
        synchronized (clientSockets) {
            clientSocket.socket.setSoTimeout(checkTimeout);
            clientSockets.add(clientSocket);
        }
    }

    @Override
    void afterConnection() throws IOException, InterruptedException {
        while (!exit) {
            Thread.sleep(100);
            synchronized (clientSockets) {
                for (ClientSocket clientSocket: clientSockets) {
                    try {
                        String message = clientSocket.in.readUTF();
                        for (ClientSocket outSocket: clientSockets) {
                            outSocket.out.writeUTF(message);
                        }
                    } catch (SocketTimeoutException e) {
                    }
                }
            }
        }
    }
}
