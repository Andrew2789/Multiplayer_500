package Java.Logic;

import Java.GUI.GameController;
import Java.GUI.GameSetupController;
import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
    public static GameServer gameServer;
    public static GameClient gameClient;
    private static Scene login, gameSetup, game;
    private static Stage stage;
    private static boolean gameShown = false;

    private static GameController gameController;
    public static GameSetupController gameSetupController;
    private static boolean connected = false;

    public static void setGameController(GameController gameController) {
        Main.gameController = gameController;
    }

    public static void setGameSetupController(GameSetupController gameSetupController) {
        Main.gameSetupController = gameSetupController;
    }

    private static void waitUntilConnected() {
        try {
            while (!connected) {
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            killThreads();
            System.exit(1);
        }
    }

    public static void createServer(int port, Runnable onFail, Runnable onSuccess) {
        connected = false;
        gameServer = new GameServer(port, 0, onFail, onSuccess, () -> connected = true, () -> {});
        gameServer.start();
        waitUntilConnected();
    }

    public static void joinGame(String name, String ipAddress, int port, Runnable onFail) {
        connected = false;
        gameClient = new GameClient(name, ipAddress, port, onFail, () -> connected = true, () -> {}, gameController);
        gameClient.start();
        waitUntilConnected();
    }

    public GameClient getGame() {
        return gameClient;
    }

    public static void killThreads() {
    	if (gameServer != null) {
            gameServer.exit();
		}
		if (gameClient != null) {
            gameClient.exit();
		}
        gameServer = null;
        gameClient = null;
    }

    public static void showLogin() {
        stage.setMinWidth(0);
        stage.setMinHeight(0);
        stage.setScene(login);
        stage.setWidth(500);
        stage.setHeight(200);
        stage.setResizable(false);
    }

    public static void showGameSetup() {
        stage.setMinWidth(400);
        stage.setMinHeight(250);
        stage.setScene(gameSetup);
        stage.setWidth(600);
        stage.setHeight(400);
        stage.setResizable(true);
    }

    public static void showGame() {
        stage.setMinWidth(600);
        stage.setMinHeight(500);
        stage.setScene(game);
        stage.setWidth(1366);
        stage.setHeight(768);
        stage.setResizable(true);
        gameController.setRoundInfoLabelText("Waiting for other party members...");
        gameShown = true;
    }

    @Override
    public void start(Stage stage) {
        /*System.setErr(new PrintStream(new BufferedOutputStream(new FileOutputStream("stderr.log")), true));
        System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream("stdout.log")), true));*/
        Main.stage = stage;
        stage.setTitle("Multiplayer 500");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/Resources/Images/club.png")));
        stage.setX(200);
        stage.setY(150);
        ChangeListener<Number> aspectRatioLimiter = (observable, oldValue, newValue) -> {
            double trickGap = stage.getHeight() - 245 - ((stage.getWidth() - 20)*0.3); //The size of the gap between the trick and the player's hand in pixels
            if (trickGap < 5 && gameShown) {
                stage.setHeight(250 + ((stage.getWidth() - 20)*0.3));
            }
        };
        stage.widthProperty().addListener(aspectRatioLimiter);
        stage.heightProperty().addListener(aspectRatioLimiter);

        try {
            login = new Scene(FXMLLoader.load(getClass().getResource("/Resources/FXML/connection.fxml")), 500, 200);
            gameSetup = new Scene(FXMLLoader.load(getClass().getResource("/Resources/FXML/gameSetup.fxml")), 600, 400);
            game = new Scene(FXMLLoader.load(getClass().getResource("/Resources/FXML/game.fxml")), 1366, 768);
            showLogin();
			stage.show();
		} catch (IOException e) {
        	e.printStackTrace();
        	stop();
		}
    }

    @Override
    public void stop() {
    	System.out.println("Exiting GUI, killing threads");
        killThreads();
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
