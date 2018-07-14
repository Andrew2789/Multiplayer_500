package Java.Logic;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public abstract class SocketThread extends Thread {
	private Thread t;
    private Runnable onFail, onSuccess, onDisconnect;
	private ServerSocket serverSocket = null;
    protected List<ClientSocket> clientSockets = new ArrayList<>();
	protected static final int checkTimeout = 100;
	protected boolean exit = false, stopWaiting = false, ownsSocket = true;
	protected String ipAddress = null;
	protected int port, connectTimeout, connections = 1;

    /**
     * Construct a new SocketThread as a client. The client will not attempt to connect to the specified server until the thread is started.
     *
     * @param ipAddress The IP address of the server to connect to.
     * @param port The port of the server to connect to.
     * @param onFail A runnable to run if the server cannot be connected to after the thread starts.
     * @param onSuccess A runnable to run if a connection is successfully established with the server.
     * @param onDisconnect A runnable to run when the socket connection is closed for whatever reason after previously being connected.
     */
	public SocketThread(String ipAddress, int port, Runnable onFail, Runnable onSuccess, Runnable onDisconnect) {
		this.ipAddress = ipAddress;
		this.port = port;
		this.connectTimeout = 10000; //10 seconds
        this.onFail = onFail;
        this.onSuccess = onSuccess;
        this.onDisconnect = onDisconnect;
	}

    /**
     * Construct a new SocketThread as a server. The server will not listen for connections until the thread is started.
     *
     * @param port
     * @param connections
     * @param onFail A runnable to run if the server cannot be started after the thread starts.
     * @param onSuccess A runnable to run if a connection is successfully established with the server.
     * @param onDisconnect A runnable to run when the socket connection is closed for whatever reason after previously being connected.
     */
	public SocketThread(int port, int connections, Runnable onFail, Runnable onSuccess, Runnable onDisconnect) {
		this.port = port;
		this.connections = connections;
        this.onFail = onFail;
        this.onSuccess = onSuccess;
        this.onDisconnect = onDisconnect;
	}

	public SocketThread(List<ClientSocket> clientSockets, Runnable onFail, Runnable onSuccess, Runnable onDisconnect) {
		this.clientSockets = clientSockets;
        this.onFail = onFail;
        this.onSuccess = onSuccess;
        this.onDisconnect = onDisconnect;
		ownsSocket = false;
	}

    public void stopWaiting() {
        stopWaiting = true;
    }

    public void exit() {
        exit = true;
    }

	abstract void afterConnection() throws InterruptedException, IOException;

	public boolean streamsReady() {
		return clientSockets.size() == connections;
	}

	public void run() {
		if (ownsSocket) { //Only set up socket connections and io streams if owner, otherwise it should already be done
			if (ipAddress != null) {
				try {
					Socket socket = new Socket();
					socket.connect(new InetSocketAddress(InetAddress.getByName(ipAddress), port), connectTimeout);
					socket.setSoTimeout(checkTimeout);
					clientSockets.add(new ClientSocket(socket));
				} catch (IOException e) {
					onFail.run();
					return;
				}
			} else {
				try {
					serverSocket = new ServerSocket(port);
					serverSocket.setSoTimeout(checkTimeout);
				} catch (IOException e) {
					serverSocket = null;
					onFail.run();
					return;
				}
			}

			try {
				//Accept connection if server
				if (serverSocket != null) {
					while (!exit && !stopWaiting && clientSockets.size() < connections) {
						try {
							Socket socket = serverSocket.accept();
							socket.setSoTimeout(checkTimeout);
                            clientSockets.add(new ClientSocket(socket));
						} catch (SocketTimeoutException e) {
						}
					}
					if (exit) {
						return;
					}
                    connections = clientSockets.size();
				}

				onSuccess.run();

				afterConnection();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			try {
				afterConnection();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}

		onDisconnect.run();

		for (ClientSocket clientSocket: clientSockets) {
		    clientSocket.tearDown();
        }
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void start() {
		if (t == null) {
			t = new Thread(this);
			t.start();
		}
	}
}
