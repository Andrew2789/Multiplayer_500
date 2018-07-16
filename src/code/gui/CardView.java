package code.gui;

import code.logic.Card;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class CardView extends ImageView {
    private Card card = null;

    public CardView() {
        super();
    }

    public void setCard(Card card, Image image) {
        this.card = card;
        setImage(image);
    }

    public Card getCard() {
        return card;
    }
}
