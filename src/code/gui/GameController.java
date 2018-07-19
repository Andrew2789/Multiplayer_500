package code.gui;

import code.game.Bid;
import code.game.Card;
import code.network.Main;
import code.game.Player;
import java.net.URL;
import java.util.*;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

public class GameController implements Initializable {
    @FXML
    private AnchorPane mainPane;
    @FXML
    private GridPane cardPane, playedCardsPane;
    @FXML
    private Pane cardSize, cardHeight;
    @FXML
    private Label roundInfoLabel, turnLabel, trickResultsLabel, roundResultsLabel;
    @FXML
    private ListView<String> roundOrderList;
    @FXML
    private Button continueButton;
    @FXML
    private BorderPane dragGuide;
    @FXML
    private SplitPane middleContainer;
    @FXML
    private TextField chatTextField;
    @FXML
    private TextArea chatTextArea;
    @FXML
    private TableView<Integer> biddingTable;

    private Map<Card, Image> cardImages = new HashMap<>();
    private List<CardView> trickView = new ArrayList<>();
    private List<Label> trickLabels = new ArrayList<>();
    private List<CardView> handView = new ArrayList<>();

    private boolean playing = false;
    private int predictorCell;
    private List<Separator> predictors = new ArrayList<>();

    private CardView beingDragged = null;

    private int trickSize = 4, handSize = 10;

    public void updateHand(List<Card> hand, List<Card> playableCards) {
        if (hand.size() > handSize) {
            System.err.println(String.format("Hand size larger than %d (%d).", handSize, hand.size()));
            return;
        }
        for (int i = 0; i < hand.size(); i++) {
            handView.get(i).setCard(hand.get(i), cardImages.get(hand.get(i)));
            handView.get(i).setVisible(true);
            handView.get(i).setOpacity(0.7);
            if (playableCards != null) {
                for (Card card : playableCards) {
                    if (handView.get(i).getCard().equals(card)) {
                        handView.get(i).setOpacity(1);
                        break;
                    }
                }
            }
        }
        for (int i = hand.size(); i < 10; i++) {
            handView.get(i).setVisible(false);
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

    public void updateRoundInfo(List<Player> players, int selfIndex, List<Integer> tricksWon, int trickNum, int roundNum) {
        Platform.runLater(() -> roundInfoLabel.setText(String.format(
            "You are playing as %s\n"
                + "Round %d, Trick %d\n\n"
                + "Team 1: %dpts, %d tricks won this round (%s & %s)\n"
                + "Team 2: %dpts, %d tricks won this round (%s & %s)", players.get(selfIndex).getName(), roundNum, trickNum,
            players.get(0).getPoints(), tricksWon.get(0), players.get(0).getName(), players.get(2).getName(),
            players.get(1).getPoints(), tricksWon.get(1), players.get(1).getName(), players.get(3).getName())));
    }

    public void setPlayerTurn(List<Player> players, List<Card> playableCards, int selfIndex, int currentPlayer) {
        Platform.runLater(() -> {
            roundOrderList.getItems().clear();
            for (int i = 0; i < players.size(); i++) {
                if (i == currentPlayer) {
                    roundOrderList.getItems().add(players.get(i).getName() + " (playing)");
                } else {
                    roundOrderList.getItems().add(players.get(i).getName());
                }
            }
            if (playableCards != null) {
                playing = true;
                turnLabel.setText("Your turn");
                roundResultsLabel.setVisible(false);
                updateHand(players.get(selfIndex).getHand(), playableCards);
            } else {
                playing = false;
                if (currentPlayer != -1) {
                    turnLabel.setText(players.get(currentPlayer).getName() + "'s turn");
                    roundResultsLabel.setVisible(false);
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
            trickResultsLabel.setText(winner.getName() + " won the trick.");
            trickResultsLabel.setVisible(true);
            continueButton.setVisible(true);
        });
    }

    public void setRoundResults(Bid bid, boolean won, int bidWinner, int trickPoints, List<Player> players) {
        Platform.runLater(() -> {
            updateTrick(new ArrayList<>(), new ArrayList<>());
            int playingTeam = bidWinner % 2,
                opposingTeam = (bidWinner + 1) % 2,
                points = won ? bid.getPoints() : -bid.getPoints();
            String roundResult = won ? "achieved" : "failed to achieve";
            roundResultsLabel.setText(String.format("Team %d (%s & %s) %s their bid of %s, earning %d points.\n"
                + "Team %d (%s & %s) gained %d pts from winning tricks.",
                playingTeam + 1, players.get(playingTeam).getName(), players.get(playingTeam + 2).getName(), roundResult, bid.toWordString(), points,
                opposingTeam + 1, players.get(opposingTeam).getName(), players.get(opposingTeam + 2).getName(), trickPoints));
            roundResultsLabel.setVisible(true);
            continueButton.setVisible(true);
        });
    }

    public void continueClicked() {
        trickResultsLabel.setVisible(false);
        continueButton.setVisible(false);
        turnLabel.setText("Waiting for other players to continue...");
        roundResultsLabel.setText("Waiting for other players to continue...");
        roundResultsLabel.setVisible(true);
        Main.gameClient.readyToContinue();
    }

    private int getNewCardIndex(double sceneX) {
        int index = 0;
        while (index < handView.size() && handView.get(index).isVisible()) {
            if (sceneX - cardSize.getWidth() * 0.5 - (10 + (cardSize.getWidth()+2)*index) <= 0) {
                return index;
            }
            index++;
        }
        return index;
    }

    private void adjustPredictors(int index, CardView cardView) {
        if (predictorCell != index || !predictors.get(1).isVisible()) {
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

    private void hidePredictors() {
        for (Separator predictor: predictors) {
            cardPane.getChildren().remove(predictors.get(1));
            if (predictor.isVisible()) {
                cardPane.getChildren().remove(predictor);
                predictor.setVisible(false);
            }
        }
    }

    private boolean isInTrickRegion(double x, double y) {
        double trickX = 10 + middleContainer.getLayoutX() + dragGuide.getLayoutX();
        double trickY = 10 + middleContainer.getLayoutY() + dragGuide.getLayoutY();
        return (
            x >= trickX && x < trickX + dragGuide.getWidth() &&
            y >= trickY && y < trickY + dragGuide.getHeight());
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
        Main.setGameController(this);

        //Load card images
        List<Character> suits = new ArrayList<>(Arrays.asList('s', 'c', 'd', 'h'));
        for (char suit: suits) {
            for (int val = 1; val < 14; val++) {
                Card card = new Card(suit, val);
                cardImages.put(card, new Image(getClass().getResourceAsStream(String.format("/resources/images/cards/%s.png", card))));
            }
        }
        Card joker = new Card('j', 0);
        cardImages.put(joker, new Image(getClass().getResourceAsStream(String.format("/resources/images/cards/%s.png", joker))));

        for (int i = 0; i < 2; i++) {
            Separator predictor = new Separator();
            predictor.setOrientation(Orientation.VERTICAL);
            predictor.setVisible(false);
            predictors.add(predictor);
        }
        GridPane.setHalignment(predictors.get(0), HPos.RIGHT);

        chatTextArea.textProperty().addListener((observable, oldValue, newValue) -> chatTextArea.setScrollTop(Double.MAX_VALUE));
        chatTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                submitChatMessage();
            }
        });

        suits.add('n');
        for (char suit: suits) {
            TableColumn<Integer, String> testCol = new TableColumn<>();
            testCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(new Bid(param.getValue(), suit).toSymbolString()));
            biddingTable.getColumns().add(testCol);
        }
        for (int i = 6; i <= 10; i++) {
            biddingTable.getItems().add(i);
        }

        /*biddingTable.setRowFactory(new Callback<TableView<String>, TableRow<String>>() {
            @Override
            public TableRow<String> call(TableView<String> tableView) {
                final TableRow<String> row = new TableRow<String>() {
                    @Override
                    public void updateItem(WaitingListItem item, boolean empty) {
                        super.updateItem(item, empty);
                        getStyleClass().remove("highlighted-row");
                        setTooltip(null);
                        if (item != null && !empty) {
                            if (SearchUtils.getUserById(item.getUserId()).getOrgans().contains(item.getOrganType())) {
                                setTooltip(new Tooltip("User is currently donating this organ"));
                                if (!getStyleClass().contains("highlighted-row")) {
                                    getStyleClass().add("highlighted-row");
                                }

                            }
                        }
                    }
                };
                //event to open receiver profile when clicked
                row.setOnMouseClicked(event -> {
                    if (!row.isEmpty() && event.getClickCount() == 2) {
                        WindowManager.newCliniciansUserWindow(SearchUtils.getUserById(row.getItem().getUserId()));
                    }
                });
                transplantTable.refresh();
                return row;
            }
        });*/

        for (int i = 0; i < handSize; i++) {
            CardView card = new CardView();
            card.setPreserveRatio(true);
            card.setSmooth(true);
            card.fitWidthProperty().bind(cardSize.widthProperty());
            GridPane.setHalignment(card, HPos.CENTER);
            card.setOnMousePressed(mouseEvent -> {
                if (beingDragged == null && card.isVisible()) {
                    beingDragged = card;
                    cardPane.getChildren().remove(card);
                    mainPane.getChildren().add(card);
                    AnchorPane.setLeftAnchor(card, mouseEvent.getSceneX() - cardSize.getWidth() * 0.5);
                    AnchorPane.setTopAnchor(card, mouseEvent.getSceneY() - cardHeight.getWidth() * 0.5);

                    adjustPredictors(handView.indexOf(card), card);
                    if (playing && card.getOpacity() > 0.9) {
                        dragGuide.setStyle("-fx-background-color: #fff; -fx-opacity: 0.4;");
                        dragGuide.toFront();
                        dragGuide.setVisible(true);
                    }
                }
            });
            card.setOnMouseDragged(mouseEvent -> {
                if (beingDragged == card) {
                    AnchorPane.setLeftAnchor(card, mouseEvent.getSceneX() - cardSize.getWidth() * 0.5);
                    AnchorPane.setTopAnchor(card, mouseEvent.getSceneY() - cardHeight.getWidth() * 0.5);

                    if (!playing || card.getOpacity() < 0.9) {
                        adjustPredictors(getNewCardIndex(mouseEvent.getSceneX()), card);
                    } else if (isInTrickRegion(mouseEvent.getSceneX(), mouseEvent.getSceneY())) {
                        dragGuide.setStyle("-fx-background-color: #dae7f1; -fx-opacity: 0.4;");
                        hidePredictors();
                    } else {
                        dragGuide.setStyle("-fx-background-color: #fff; -fx-opacity: 0.4;");
                        adjustPredictors(getNewCardIndex(mouseEvent.getSceneX()), card);
                    }
                }
            });
            card.setOnMouseReleased(mouseEvent -> {
                if (beingDragged == card) {
                    dragGuide.setVisible(false);
                    mainPane.getChildren().remove(card);
                    cardPane.add(card, handView.indexOf(card), 0);
                    beingDragged = null;

                    hidePredictors();
                    if (playing && card.getOpacity() > 0.9 && isInTrickRegion(mouseEvent.getSceneX(), mouseEvent.getSceneY())) {
                        Main.gameClient.cardPlayed(card.getCard());
                    } else {
                        Main.gameClient.moveCard(handView.indexOf(card), getNewCardIndex(mouseEvent.getSceneX()));
                    }
                }
            });
            cardPane.add(card, i, 0);
            handView.add(card);
        }

        for (int i = 1; i < 1 + trickSize; i++) {
            CardView card = new CardView();
            card.setPreserveRatio(true);
            card.setSmooth(true);
            card.fitWidthProperty().bind(cardSize.widthProperty());
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
