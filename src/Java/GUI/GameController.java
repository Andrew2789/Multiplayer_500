package Java.GUI;

import Java.Logic.Card;
import Java.Logic.GameClient;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.*;

public class GameController implements Initializable {
    @FXML
    private GridPane mainPane, cardPane;
    @FXML
    private Pane gridSize;

    private Map<Card, Image> cardImages = new HashMap<>();

    private List<Card> hand = new ArrayList<>();
    private List<ImageView> handView = new ArrayList<>();

    private GameClient client;

    public void updateHand(List<Card> hand) {
        this.hand = hand;
        for (int i = 0; i < hand.size(); i++) {
            handView.get(i).setImage(cardImages.get(hand.get(i)));
            System.out.println("setting " + i);
        }
        for (int i = hand.size(); i < 10; i++) {
            handView.get(i).setImage(null);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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

        for (int i = 0; i < 10; i++) {
            ImageView card = new ImageView();
            card.setPreserveRatio(true);
            card.setSmooth(true);
            card.fitWidthProperty().bind(gridSize.widthProperty());
            final int index = i;
            card.setOnMouseClicked(event -> {
                if (hand.size() > index) {
                    card.setStyle("-fx-border-width: 2px; -fx-border-radius: 4px; -fx-border-color: #ffffff");
                    //client.cardPlayed(hand.get(index));
                }
            });
            cardPane.add(card, i, 0);
            handView.add(card);
        }

        mainPane.getRowConstraints().get(2).maxHeightProperty().bind(handView.get(0).fitHeightProperty());
        mainPane.getRowConstraints().get(2).prefHeightProperty().bind(handView.get(0).fitHeightProperty());

        List<Card> newhand = new ArrayList<>();
        newhand.add(new Card("d1"));
        newhand.add(new Card("d13"));
        newhand.add(new Card("s11"));
        newhand.add(new Card("c12"));
        newhand.add(new Card("j0"));
        newhand.add(new Card("h6"));

        updateHand(newhand);
    }
}
