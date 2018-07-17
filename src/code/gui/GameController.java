package code.gui;

import code.logic.Card;
import code.logic.Main;
import code.logic.Player;

import java.awt.event.MouseEvent;
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
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class GameController implements Initializable {
    @FXML
    private AnchorPane mainPane;
    @FXML
    private GridPane cardPane, playedCardsPane;
    @FXML
    private Pane gridSize, cardHeight;
    @FXML
    private Label roundInfoLabel, turnLabel, trickResultLabel, dragGuideLabel;
    @FXML
    private ListView<String> roundOrderList;
    @FXML
    private Button endTrickButton;
    @FXML
    private BorderPane dragGuide;

    private Map<Card, Image> cardImages = new HashMap<>();

    private List<CardView> trickView = new ArrayList<>();
    private List<Label> trickLabels = new ArrayList<>();
    private List<CardView> handView = new ArrayList<>();

    private int predictorCell;
    private List<Separator> predictors = new ArrayList<>();

    private CardView beingDragged = null;

    private int trickSize = 4, handSize = 10;

    public void updateHand(List<Card> hand) {
        if (hand.size() > handSize) {
            System.err.println(String.format("Hand size larger than %d (%d).", handSize, hand.size()));
            return;
        }
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i) != null) {
                handView.get(i).setCard(hand.get(i), cardImages.get(hand.get(i)));
                handView.get(i).setVisible(true);
            } else {
                handView.get(i).setVisible(false);
            }
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
        Platform.runLater(() -> roundInfoLabel.setText(String.format(
            "You are playing as %s\n"
                + "Round 1, Trick %d\n\n"
                + "%s & %s: %d tricks won, %dpts\n"
                + "%s & %s: %d tricks won, %dpts", players.get(selfIndex).getName(), trickNum,
            players.get(0).getName(), players.get(2).getName(), tricksWon.get(0), players.get(0).getPoints(),
            players.get(1).getName(), players.get(3).getName(), tricksWon.get(1), players.get(1).getPoints())));
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
                    cardView.setOpacity(0.7);
                    for (Card card: playableCards) {
                        if (cardView.getCard().equals(card)) {
                            cardView.setOpacity(1);
                            break;
                        }
                    }
                }
            } else {
                if (index != -1) {
                    turnLabel.setText(players.get(index).getName() + "'s turn");
                }
                for (CardView cardView: handView) {
                    cardView.setOpacity(0.7);
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
                cardView.setOpacity(0.7);
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

    private int getNewCardIndex(double sceneX) {
        int index = 0;
        while (index < handView.size() && handView.get(index).isVisible()) {
            if (sceneX - gridSize.getWidth() * 0.5 - (10 + (gridSize.getWidth()+2)*index) <= 0) {
                return index;
            }
            index++;
        }
        return index;
    }

    private void adjustPredictors(int index, CardView cardView) {
        if (predictorCell != index) {
            boolean centred = false;
            if (handView.indexOf(cardView) == index || handView.indexOf(cardView) == index - 1) {
                centred = true;
                GridPane.setHalignment(predictors.get(1), HPos.CENTER);
                predictors.get(1).setStyle("-fx-border-width: 2px; -fx-border-color: #4077a5;");
                predictorCell = handView.indexOf(cardView) == index ? index : index - 1;
            } else {
                GridPane.setHalignment(predictors.get(1), HPos.LEFT);
                predictors.get(1).setStyle("-fx-border-width: 1px; -fx-border-color: #4077a5;");
                predictorCell = index;
            }

            cardPane.getChildren().remove(predictors.get(0));
            cardPane.getChildren().remove(predictors.get(1));

            cardPane.add(predictors.get(1), predictorCell, 0);
            predictors.get(1).setVisible(true);

            predictors.get(0).setVisible(predictorCell != 0 && !centred);
            if (predictorCell != 0 && !centred) {
                cardPane.add(predictors.get(0), predictorCell - 1, 0);
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Main.setGameController(this);

        for (int i = 0; i < 2; i++) {
            Separator predictor = new Separator();
            predictor.setOrientation(Orientation.VERTICAL);
            predictor.setVisible(false);
            predictors.add(predictor);
        }
        GridPane.setHalignment(predictors.get(0), HPos.RIGHT);

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
            CardView card = new CardView();
            card.setPreserveRatio(true);
            card.setSmooth(true);
            card.fitWidthProperty().bind(gridSize.widthProperty());
            GridPane.setHalignment(card, HPos.CENTER);
            card.setOnMousePressed(mouseEvent -> {
                if (beingDragged == null) {
                    dragGuide.setVisible(true);
                    beingDragged = card;
                    cardPane.getChildren().remove(card);
                    mainPane.getChildren().add(card);
                    AnchorPane.setLeftAnchor(card, mouseEvent.getSceneX() - gridSize.getWidth() * 0.5);
                    AnchorPane.setTopAnchor(card, mouseEvent.getSceneY() - cardHeight.getWidth() * 0.5);

                    adjustPredictors(handView.indexOf(card), card);
                }
            });
            card.setOnMouseDragged(mouseEvent -> {
                if (beingDragged == card) {
                    AnchorPane.setLeftAnchor(card, mouseEvent.getSceneX() - gridSize.getWidth() * 0.5);
                    AnchorPane.setTopAnchor(card, mouseEvent.getSceneY() - cardHeight.getWidth() * 0.5);

                    adjustPredictors(getNewCardIndex(mouseEvent.getSceneX()), card);
                }
            });
            card.setOnMouseReleased(mouseEvent -> {
                if (beingDragged == card) {
                    dragGuide.setVisible(false);
                    mainPane.getChildren().remove(card);
                    cardPane.add(card, handView.indexOf(card), 0);
                    beingDragged = null;

                    cardPane.getChildren().remove(predictors.get(1));
                    if (predictors.get(0).isVisible()) {
                        cardPane.getChildren().remove(predictors.get(0));
                    }
                    predictors.get(0).setVisible(false);
                    predictors.get(1).setVisible(false);

                    Main.gameClient.moveCard(handView.indexOf(card), getNewCardIndex(mouseEvent.getSceneX()));
                }
                if (card.isVisible() && card.getOpacity() > 0.9) {
                    Main.gameClient.cardPlayed(card.getCard());
                }
            });
            cardPane.add(card, i, 0);
            handView.add(card);
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
