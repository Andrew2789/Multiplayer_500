package Java.GUI;

import Java.Logic.Main;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

public class ConnectionController implements Initializable {
    @FXML
    private TextField ipInput, clientPortInput, hostPortInput;
    @FXML
    private Button connectButton, hostButton;

    private boolean connected = false;

    private static final int PORT_MIN = 1024;

    public void connectClicked() {
        String ipAddress = ipInput.getCharacters().toString();
        int port = Integer.parseInt(clientPortInput.getCharacters().toString());

        connected = false;
        Main.joinGame(ipAddress, port, () -> System.out.println("failed to connect to host"),() -> connected = true);
        try {
            while (!connected) {
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            Main.killThreads();
            System.exit(1);
        }
        Main.showGame();
    }

    public void hostClicked() {
        int port = Integer.parseInt(hostPortInput.getCharacters().toString());
        Main.createServer(port, () -> System.out.println("failed to host"), () -> System.out.println("server up"));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Main.killThreads();
            System.exit(1);
        }
        connected = false;
        Main.joinGame("127.0.0.1", port, () -> System.out.println("failed to connect to host"),() -> connected = true);
        try {
            while (!connected) {
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            Main.killThreads();
            System.exit(1);
        }
        Main.showGame();
    }

    private boolean updatePortField(String newValue) {
        if (!newValue.isEmpty()) {
            try {
                int fieldValue = Integer.parseInt(newValue);
                if (fieldValue < 0 || fieldValue > 65536 || newValue.charAt(0) == '0') {
                    return true;
                }
            } catch (NumberFormatException e) {
                return true;
            }
        }
        return false;
    }

    private boolean clientConnectReady() {
        String ipAddress = ipInput.getCharacters().toString();
        String port = clientPortInput.getCharacters().toString();
        if (ipAddress.isEmpty() || port.isEmpty())
            return false;

        int portValue = Integer.parseInt(port);
        if (portValue < PORT_MIN || (ipAddress.length() - ipAddress.replace(".", "").length() != 3))
            return false;

        return ipAddress.lastIndexOf('.') != ipAddress.length() - 1;
    }

    private boolean hostListenReady() {
        try {
            return Integer.parseInt(hostPortInput.getCharacters().toString()) >= PORT_MIN;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void initialize(URL location, ResourceBundle resources) {
        ipInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (clientConnectReady()) {
                    connectClicked();
                }
            }
        });

        clientPortInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (clientConnectReady()) {
                    connectClicked();
                }
            }
        });

        hostPortInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (hostListenReady()) {
                    hostClicked();
                }
            }
        });

        ipInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                String[] octets = newValue.split("\\.");
                int numPeriods = newValue.length() - newValue.replace(".", "").length();
                if (octets.length > 4 || numPeriods > 3 || numPeriods > octets.length) {
                    ((StringProperty) observable).setValue(oldValue);
                    return;
                }
                try {
                    for (int i = 0; i < octets.length; i++)
                        if (octets[i].isEmpty()) {
                            if (i != octets.length - 1) {
                                ((StringProperty) observable).setValue(oldValue);
                                break;
                            }
                        } else {
                            int octetValue = Integer.parseInt(octets[i]);
                            if (octetValue < 0 || octetValue > 255 || newValue.charAt(0) == '0') {
                                ((StringProperty) observable).setValue(oldValue);
                                break;
                            }
                        }
                } catch (NumberFormatException e) {
                    ((StringProperty) observable).setValue(oldValue);
                }
            }
            connectButton.setDisable(!clientConnectReady());
        });

        clientPortInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (updatePortField(newValue)) {
                ((StringProperty) observable).setValue(oldValue);
            }
            connectButton.setDisable(!clientConnectReady());
        });

        hostPortInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (updatePortField(newValue)) {
                ((StringProperty) observable).setValue(oldValue);
            }
            hostButton.setDisable(!hostListenReady());
        });
    }
}
