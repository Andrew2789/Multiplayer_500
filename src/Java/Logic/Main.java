package Java.Logic;

import Java.GUI.GameController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class Main extends Application {
    private static GameServer gameServer;
    private static GameClient gameClient;
    private static Scene scene;

    public static void createServer(int port, Runnable onFail, Runnable onSuccess) {
        gameServer = new GameServer(port, 1, onFail, onSuccess, () -> {});
        gameServer.start();
    }

    public static GameClient joinGame(String ipAddress, int port, Runnable onFail, Runnable onSuccess, GameController gameController) {
        gameClient = new GameClient(ipAddress, port, onFail, onSuccess, () -> {}, gameController);
        gameClient.start();
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

    @Override
    public void start(Stage stage) {
        /*System.setErr(new PrintStream(new BufferedOutputStream(new FileOutputStream("stderr.log")), true));
        System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream("stdout.log")), true));*/
        stage.setTitle("Multiplayer 500");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/Resources/Images/club.png")));
        stage.setMinWidth(600);
        stage.setMinHeight(500);
        try {
			//location = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getAbsolutePath();
			Parent root = FXMLLoader.load(getClass().getResource("/Resources/FXML/frame.fxml"));
			scene = new Scene(root, 1366, 768);
			stage.setScene(scene);
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
