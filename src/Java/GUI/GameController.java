package Java.GUI;

import Java.Logic.Card;
import Java.Logic.GameClient;
import Java.Logic.Main;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javax.swing.text.html.ListView;
import java.net.URL;
import java.util.*;

public class GameController implements Initializable {
    @FXML
    private GridPane cardPane, playedCardsPane, mainPane;
    @FXML
    private Pane gridSize;
    @FXML
    private Label roundInfoLabel;
    @FXML
    private ListView roundOrderList;

    private Map<Card, Image> cardImages = new HashMap<>();

    private List<CardView> trickView = new ArrayList<>();
    private List<CardView> handView = new ArrayList<>();
    private List<Pane> handViewBackgrounds = new ArrayList<>();

    private GameClient gameClient;

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

    public void updateTrick(List<Card> trick) {
        if (trick.size() > trickSize) {
            System.err.println(String.format("Trick size larger than %d (%d).", trickSize, trick.size()));
            return;
        }
        for (int i = 0; i < trick.size(); i++) {
            trickView.get(i).setCard(trick.get(i), cardImages.get(trick.get(i)));
            trickView.get(i).setVisible(true);
        }
        for (int i = trick.size(); i < 4; i++) {
            trickView.get(i).setVisible(false);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources){
        //Load card images
        final char[] suits = {'s', 'c', 'd', 'h'};
        for (char suit: suits) {
            for (int val = 1; val < 14; val++) {
                Card card = new Card(suit, val);
                cardImages.put(card, new Image(getClass().getResourceAsStream(String.format("/Resources/Images/Cards/%s.png", card))));
            }
        }
        Card joker = new Card('j', 0);
        cardImages.put(joker, new Image(getClass().getResourceAsStream(String.format("/Resources/Images/Cards/%s.png", joker))));

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
                if (card.isVisible()) {
                    background.setStyle("-fx-background-color: #4785b8; -fx-background-radius: 8%; -fx-background-insets: 1px 1px 1px 1px; -fx-padding: 1px 1px 1px 1px;");
                    //client.cardPlayed(hand.get(index));
                }
            });
            card.setOnMouseReleased(event -> {
                if (card.isVisible()) {
                    background.setStyle("-fx-background-color: #000; -fx-background-radius: 8%; -fx-background-insets: 1px 1px 1px 1px; -fx-padding: 1px 1px 1px 1px;");
                    gameClient.cardPlayed(card.getCard());
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
            playedCardsPane.add(card, i, 0);
            trickView.add(card);
        }


        int port = 1236;
        Main.createServer(port, () -> System.out.println("failed to host"), () -> serverUp = true);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Main.killThreads();
            System.exit(1);
        }
        System.out.println("hosted");
        gameClient = Main.joinGame("127.0.0.1", port, () -> System.out.println("failed to connect to host"), () -> gameJoined = true, this);
        while (!gameJoined && !serverUp) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Main.killThreads();
                System.exit(1);
            }
        }
        System.out.println("connected");

        //cardPane.prefHeightProperty().bind(handViewBackgrounds.get(0).heightProperty());

    }
}
