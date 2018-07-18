package code.gui;

import code.network.Main;
import code.game.Player;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

public class GameSetupController implements Initializable {
    @FXML
    private ListView<Player> team1List, team2List;
    @FXML
    private Button moveToTeam1Button, moveToTeam2Button, continueButton;
    @FXML
    private Label statusLabel;
    @FXML
    private TextField chatTextField;
    @FXML
    private TextArea chatTextArea;

    private boolean movingPlayer = false;

    public void continueClicked() {
        Main.gameServer.setTeams(team1List.getItems(), team2List.getItems());
        Main.showGame();
    }

    private void checkContinueValid() {
        continueButton.setDisable(team1List.getItems().size() != 2 || team2List.getItems().size() != 2);
    }

    public void moveToTeam1() {
        movePlayer(team2List, team1List);
    }

    public void moveToTeam2() {
        movePlayer(team1List, team2List);
    }

    private void movePlayer(ListView<Player> from, ListView<Player> to) {
        movingPlayer = true;
        Player toMove = from.getItems().remove(from.getSelectionModel().getSelectedIndex());
        to.getItems().add(toMove);
        checkSelections();
        movingPlayer = false;
        checkContinueValid();
    }

    private void checkSelectionNumber(int teamNumberClicked) {
        if (!(team1List.getSelectionModel().getSelectedItem() == null) && !(team2List.getSelectionModel().getSelectedItem() == null)) {
            if (teamNumberClicked == 1) {
                team2List.getSelectionModel().clearSelection();
            } else {
                team1List.getSelectionModel().clearSelection();
            }
        }
    }

    private void checkSelections() {
        moveToTeam1Button.setDisable(team2List.getSelectionModel().getSelectedItem() == null);
        moveToTeam2Button.setDisable(team1List.getSelectionModel().getSelectedItem() == null);
    }

    public void addPlayer(Player player) {
        Platform.runLater(() -> {
            team1List.getItems().add(player);
            if (team1List.getItems().size() + team2List.getItems().size() == 4) {
                statusLabel.setText("Allocate 2 players to each team to continue");
            }
            checkContinueValid();
        });
    }

    public void addChatMessage(String message) {
        Platform.runLater(() -> {
            if (chatTextArea.getText().isEmpty()) {
                chatTextArea.setText(message);
            } else {
                chatTextArea.setText(chatTextArea.getText() + "\n" + message);
            }
        });
    }

    private void submitChatMessage() {
        if (!chatTextField.getText().isEmpty()) {
            Main.gameClient.submitChatMessage(chatTextField.getText());
            chatTextField.setText("");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Main.setGameSetupController(this);

        chatTextArea.textProperty().addListener((observable, oldValue, newValue) -> chatTextArea.setScrollTop(Double.MAX_VALUE));
        chatTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                submitChatMessage();
            }
        });

        team1List.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!movingPlayer) {
                checkSelectionNumber(1);
            }
            checkSelections();
        });
        team2List.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!movingPlayer) {
                checkSelectionNumber(2);
            }
            checkSelections();
        });
    }
}
