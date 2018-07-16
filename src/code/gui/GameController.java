package code.gui;

import code.logic.Card;
import code.logic.Main;
import code.logic.Player;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class GameController implements Initializable {
    @FXML
    private GridPane cardPane, playedCardsPane;
    @FXML
    private Pane gridSize;
    @FXML
    private Label roundInfoLabel, turnLabel, trickResultLabel;
    @FXML
    private ListView<String> roundOrderList;
    @FXML
    private Button endTrickButton;

    private Map<Card, Image> cardImages = new HashMap<>();

    private List<CardView> trickView = new ArrayList<>();
    private List<Label> trickLabels = new ArrayList<>();
    private List<CardView> handView = new ArrayList<>();
    private List<Pane> handViewBackgrounds = new ArrayList<>();

    private int trickSize = 4, handSize = 10;

    private boolean serverUp = false, gameJoined = false;

    public void updateHand(List<Card> hand) {
        if (hand.size() > handSize) {
            System.err.println(String.format("Trick size larger than %d (%d).", handSize, hand.size()));
            return;
        }
        for (int i = 0; i < hand.size(); i++) {
            handView.get(i).setCard(hand.get(i), cardImages.get(hand.get(i)));
            handViewBackgrounds.get(i).setVisible(true);
        }
        for (int i = hand.size(); i < handSize; i++) {
            handViewBackgrounds.get(i).setVisible(false);
        }
    }

    public void updateTrick(List<Card> trick, List<Player> trickPlayers) {
        Platform.runLater(() -> {
            if (trick.size() > trickSize) {
                System.err.println(String.format("Trick size larger than %d (%d).", trickSize, trick.size()));
                return;
            }
            for (int i = 0; i < trick.size(); i++) {
                trickView.get(i).setCard(trick.get(i), cardImages.get(trick.get(i)));
                trickView.get(i).setVisible(true);
                trickLabels.get(i).setText(trickPlayers.get(i).getName());
                trickLabels.get(i).setVisible(true);
            }
            for (int i = trick.size(); i < 4; i++) {
                trickView.get(i).setVisible(false);
                trickLabels.get(i).setVisible(false);
            }
        });
    }

    public void updateRoundInfo(List<Player> players, int selfIndex, List<Integer> tricksWon, int trickNum) {
        Platform.runLater(() -> {
            roundInfoLabel.setText(String.format(
                "You are playing as %s\n"
                    + "Round 1, Trick %d\n\n"
                    + "%s & %s: %d tricks won, %dpts\n"
                    + "%s & %s: %d tricks won, %dpts", players.get(selfIndex).getName(), trickNum,
                players.get(0).getName(), players.get(2).getName(), tricksWon.get(0), players.get(0).getPoints(),
                players.get(1).getName(), players.get(3).getName(), tricksWon.get(1), players.get(1).getPoints()));
        });
    }

    public void setPlayerTurn(List<Player> players, List<Card> playableCards, int index) {
        Platform.runLater(() -> {
            roundOrderList.getItems().clear();
            for (int i = 0; i < players.size(); i++) {
                if (i == index) {
                    roundOrderList.getItems().add(players.get(i).getName() + " (playing)");
                } else {
                    roundOrderList.getItems().add(players.get(i).getName());
                }
            }
            if (playableCards != null) {
                turnLabel.setText("Your turn");
                for (CardView cardView : handView) {
                    cardView.setOpacity(0.6);
                    for (Card card: playableCards) {
                        if (cardView.getCard().equals(card)) {
                            cardView.setOpacity(0.9);
                            break;
                        }
                    }
                }
            } else {
                if (index != -1) {
                    turnLabel.setText(players.get(index).getName() + "'s turn");
                }
                for (CardView cardView: handView) {
                    cardView.setOpacity(0.6);
                }
            }
            turnLabel.setVisible(true);
        });
    }

    public void setPlayerList(List<Player> players) {
        Platform.runLater(() -> {
            roundOrderList.getItems().clear();
            for (Player player: players) {
                roundOrderList.getItems().add(player.getName());
            }
        });
    }

    public void setRoundInfoLabelText(String text) {
        Platform.runLater(() -> roundInfoLabel.setText(text));
    }

    public void setTrickResults(Player winner) {
        Platform.runLater(() -> {
            for (CardView cardView: handView) {
                cardView.setOpacity(0.6);
            }
            trickResultLabel.setText(winner.getName() + " won the trick.");
            trickResultLabel.setVisible(true);
            endTrickButton.setVisible(true);
        });
    }

    public void endTrick() {
        trickResultLabel.setVisible(false);
        endTrickButton.setVisible(false);
        Main.gameClient.readyForNextTrick();
        turnLabel.setText("Waiting for other players to continue...");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Main.setGameController(this);

        //Load card images
        final char[] suits = {'s', 'c', 'd', 'h'};
        for (char suit: suits) {
            for (int val = 1; val < 14; val++) {
                Card card = new Card(suit, val);
                cardImages.put(card, new Image(getClass().getResourceAsStream(String.format("/resources/images/cards/%s.png", card))));
            }
        }
        Card joker = new Card('j', 0);
        cardImages.put(joker, new Image(getClass().getResourceAsStream(String.format("/resources/images/cards/%s.png", joker))));

        for (int i = 0; i < handSize; i++) {
            Pane background = new StackPane();
            //background.setPrefHeight(0);
            background.setStyle("-fx-background-color: #000; -fx-background-radius: 8%; -fx-background-insets: 1px 1px 1px 1px; -fx-padding: 1px 1px 1px 1px;");
            background.setPrefHeight(0);

            CardView card = new CardView();
            card.setPreserveRatio(true);
            card.setSmooth(true);
            card.fitWidthProperty().bind(gridSize.widthProperty());
            StackPane.setAlignment(card, Pos.CENTER);
            card.setOnMousePressed(event -> {
                if (card.isVisible() && card.getOpacity() > 0.8) {
                    background.setStyle("-fx-background-color: #4785b8; -fx-background-radius: 8%; -fx-background-insets: 1px 1px 1px 1px; -fx-padding: 1px 1px 1px 1px;");
                    //client.cardPlayed(hand.get(index));
                }
            });
            card.setOnMouseReleased(event -> {
                if (card.isVisible() && card.getOpacity() > 0.8) {
                    background.setStyle("-fx-background-color: #000; -fx-background-radius: 8%; -fx-background-insets: 1px 1px 1px 1px; -fx-padding: 1px 1px 1px 1px;");
                    Main.gameClient.cardPlayed(card.getCard());
                }
            });
            background.getChildren().add(card);
            cardPane.add(background, i, 0);
            handView.add(card);
            handViewBackgrounds.add(background);
        }

        for (int i = 1; i < 1 + trickSize; i++) {
            CardView card = new CardView();
            card.setPreserveRatio(true);
            card.setSmooth(true);
            card.fitWidthProperty().bind(gridSize.widthProperty());
            playedCardsPane.add(card, i, 1);
            Label label = new Label();
            GridPane.setHalignment(label, HPos.CENTER);
            GridPane.setValignment(label, VPos.BOTTOM);
            label.setStyle("-fx-font-size: 14px; -fx-text-fill: #dae7f1");
            playedCardsPane.add(label, i, 0);
            trickView.add(card);
            trickLabels.add(label);
        }
    }
}
