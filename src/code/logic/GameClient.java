package code.logic;

import code.gui.GameController;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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

    public void moveCard(int oldIndex, int newIndex) {
        game.getPlayer(playerIndex).moveCard(oldIndex, newIndex);
        gameController.updateHand(game.getPlayer(playerIndex).getHand());
    }

    private boolean initializeRound(DataInputStream in) throws IOException {
        if (!receiveBool(in)) {
            return false; //Game over
        }
        game.getPlayer(playerIndex).setHand(receiveCardList(in));
        gameController.updateHand(game.getPlayer(playerIndex).getHand());
        game.hostDealt();

        return true;
    }

    public void readyForNextTrick() {
        readyForNextTrick = true;
    }

    private void bid(DataInputStream in, DataOutputStream out) throws IOException, InterruptedException {
        game.setBid(new Bid(receiveString(in)), 2);
    }

    private void playRound(DataInputStream in, DataOutputStream out) throws IOException, InterruptedException {
        int trickNumber = 1;
        boolean trumpsPlayed = false;
        for (int i = 0; i < 10; i++) {
            //Prepare for trick
            gameController.updateTrick(game.getTrick(), game.getTrickPlayers());
            gameController.updateRoundInfo(game.getPlayers(), playerIndex, game.getTricksWon(), trickNumber);

            //Play trick and display results
            trumpsPlayed = playTrick(in, out, trumpsPlayed);
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

    private boolean playTrick(DataInputStream in, DataOutputStream out, boolean trumpsPlayed) throws IOException, InterruptedException {
        int playerTurn = receiveInt(in);
        while (playerTurn != -1) {
            gameController.setPlayerTurn(game.getPlayers(), game.getBid().filterPlayable(game.getPlayer(playerTurn).getHand(), trumpsPlayed, game.getLeadingSuit()), playerTurn);
            playing = false;
            if (playerTurn == playerIndex) {
                playing = true;
                while (playing) {
                    sleep(100);
                }
                out.writeUTF(cardPlayed.toString());
                playing = true;
            }

            Card played = new Card(receiveString(in));
            game.cardPlayed(played, playerTurn, playing);
            if (played.getSuit() == game.getBid().getTrumpSuit()) {
                trumpsPlayed = true;
            }
            gameController.updateTrick(game.getTrick(), game.getTrickPlayers());
            if (playerTurn == playerIndex) {
                gameController.updateHand(game.getPlayer(playerIndex).getHand());
            }
            playerTurn = receiveInt(in);
        }
        game.findTrickWinner();
        Player winner = game.getPlayer(receiveInt(in));
        gameController.setTrickResults(winner);
        gameController.setPlayerTurn(game.getPlayers(), null, -1);
        return trumpsPlayed;
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
            if (exit) {
                return null;
            }
        }
        return cards;
    }
}
