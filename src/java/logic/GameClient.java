package java.logic;

import java.gui.GameController;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameClient extends SocketThread {
    private GameController gameController;
    private String name;
    private Card cardPlayed = null;
    private boolean playing, readyForNextTrick;

    private int playerIndex;
    private Game game;

    public GameClient(String name, String ipAddress, int port, Runnable onFail, Runnable onSuccess, Runnable onDisconnect, GameController gameController) {
        super(ipAddress, port, onFail, onSuccess, onDisconnect);
        this.gameController = gameController;
        this.name = name;
    }

    public void cardPlayed(Card card) {
        if (playing) {
            cardPlayed = card;
            playing = false;
        }
    }

    private boolean initializeRound(DataInputStream in) throws IOException {
        if (!receiveBool(in)) {
            return false; //Game over
        }

        game.getPlayer(playerIndex).setHand(receiveCardList(in));
        gameController.updateHand(game.getPlayer(playerIndex).getHand());

        return true;
    }

    public void readyForNextTrick() {
        readyForNextTrick = true;
    }

    private void bid(DataInputStream in, DataOutputStream out) throws IOException, InterruptedException {
        game.setBid(new Bid(receiveString(in)));
    }

    private void playRound(DataInputStream in, DataOutputStream out) throws IOException, InterruptedException {
        int trickNumber = 1;
        for (int i = 0; i < 10; i++) {
            //Prepare for trick
            gameController.updateTrick(game.getTrick(), new ArrayList<>());
            gameController.updateRoundInfo(game.getPlayers(), playerIndex, game.getTricksWon(), trickNumber);

            //Play trick and display results
            playTrick(in, out);
            gameController.updateRoundInfo(game.getPlayers(), playerIndex, game.getTricksWon(), trickNumber);

            //Wait for player to click continue
            readyForNextTrick = false;
            while (!readyForNextTrick) {
                sleep(100);
                if (exit) {
                    return;
                }
            }
            out.writeBoolean(true);
            trickNumber++;
        }
    }

    private void playTrick(DataInputStream in, DataOutputStream out) throws IOException, InterruptedException {
        List<Player> trickPlayers = new ArrayList<>();
        int playerTurn = receiveInt(in);
        Character leadingSuit = null;
        while (playerTurn != -1) {
            gameController.setPlayerTurn(game.getPlayers(), game.getBid().filterPlayable(game.getPlayer(playerTurn).getHand(), leadingSuit), playerTurn);
            playing = false;
            if (playerTurn == playerIndex) {
                playing = true;
                while (playing) {
                    Thread.sleep(100);
                }
                out.writeUTF(cardPlayed.toString());
                playing = true;
            }

            Card played = new Card(receiveString(in));
            if (leadingSuit == null) {
                leadingSuit = played.getSuit();
            }
            game.cardPlayed(played, playerTurn, playing);
            gameController.updateTrick(game.getTrick(), trickPlayers);
            if (playerTurn == playerIndex) {
                gameController.updateHand(game.getPlayer(playerIndex).getHand());
            }
            playerTurn = receiveInt(in);
        }
        game.findTrickWinner();
        Player winner = game.getPlayer(receiveInt(in));
        gameController.setTrickResults(winner);
        gameController.setPlayerTurn(game.getPlayers(), null, -1);
    }

    @Override
    void afterConnection() throws IOException, InterruptedException {
        gameController.updateHand(new ArrayList<>());
        DataInputStream in = clientSockets.get(0).in;
        DataOutputStream out = clientSockets.get(0).out;

        out.writeUTF(name);
        receiveBool(in);
        gameController.setRoundInfoLabelText("Waiting the host to choose teams...");
        playerIndex = receiveInt(in);

        int numPlayers = receiveInt(in);
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < numPlayers; i++) {
            players.add(new Player(receiveString(in)));
        }
        gameController.setPlayerList(players);
        game = new Game(players);

        for (Player player: game.getPlayers()) {
            player.resetPoints();
        }
        while (initializeRound(in)) {
            bid(in, out);
            if (exit) {
                return;
            }
            playRound(in, out);
            if (exit) {
                return;
            }
        }
    }

    //Data transfer utility methods

    private List<Card> receiveCardList(DataInputStream in) throws IOException {
        List<Card> cards = new ArrayList<>();
        int length = receiveInt(in);
        for (int i = 0; i < length; i++) {
            cards.add(new Card(receiveString(in)));
        }
        return cards;
    }
}
