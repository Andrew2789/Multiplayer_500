package Java.Logic;

import Java.GUI.GameController;
import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
    private static GameServer gameServer;
    public static GameClient gameClient;
    private static Scene login, game;
    private static Stage stage;

    private static GameController gameController;

    public static void setGameController(GameController gameController) {
        Main.gameController = gameController;
    }

    public static void createServer(int port, Runnable onFail, Runnable onSuccess) {
        gameServer = new GameServer(port, 1, onFail, onSuccess, () -> {});
        gameServer.start();
    }

    public static void joinGame(String ipAddress, int port, Runnable onFail, Runnable onSuccess) {
        gameClient = new GameClient(ipAddress, port, onFail, onSuccess, () -> {}, gameController);
        gameClient.start();
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
        stage.setWidth(500);
        stage.setHeight(160);
        stage.setResizable(false);
        stage.setScene(login);
    }

    public static void showGame() {
        stage.setMinWidth(600);
        stage.setMinHeight(500);
        stage.setWidth(1366);
        stage.setHeight(768);
        stage.setResizable(true);
        stage.setScene(game);
    }

    @Override
    public void start(Stage stage) {
        /*System.setErr(new PrintStream(new BufferedOutputStream(new FileOutputStream("stderr.log")), true));
        System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream("stdout.log")), true));*/
        Main.stage = stage;
        stage.setTitle("Multiplayer 500");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/Resources/Images/club.png")));
        try {
            login = new Scene(FXMLLoader.load(getClass().getResource("/Resources/FXML/connection.fxml")), 500, 160);
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
    	System.out.println("Exiting GUI, killing transfer threads");
        killThreads();
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
