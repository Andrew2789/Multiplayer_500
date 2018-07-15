package Java.Logic;

import Java.GUI.GameController;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameClient extends SocketThread {
    private GameController gameController;
    private String name;
    private Card cardPlayed = null;
    private boolean playing = false, readyForNextTrick = false;

    private int playerIndex;
    private List<Player> players;
    private int trickNumber;
    private List<Card> trick;

    public GameClient(String name, String ipAddress, int port, Runnable onFail, Runnable onSuccess, Runnable onDisconnect, GameController gameController) {
        super(ipAddress, port, onFail, onSuccess, onDisconnect);
        this.gameController = gameController;
        this.name = name;
    }

    public void cardPlayed(Card card) {
        if (playing) {
            if (players.get(playerIndex).hand.remove(card)) {
                cardPlayed = card;
                playing = false;
            }
        }
    }

    private void initializeGame(DataInputStream in) throws IOException {
        for (Player player: players) {
            player.points = 0;
            player.hand = new ArrayList<>();
        }
    }

    private boolean initializeRound(DataInputStream in) throws IOException {
        trickNumber = 0;
        if (!receiveBool(in)) {
            return false; //Game over
        }

        for (Player player: players) {
            player.hand = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                player.hand.add(null);
            }
        }
        players.get(playerIndex).hand = receiveCardList(in);
        gameController.updateHand(players.get(playerIndex).hand);

        return true;
    }

    public void readyForNextTrick() {
        readyForNextTrick = true;
    }

    private void playRound(DataInputStream in, DataOutputStream out) throws IOException, InterruptedException {
        List<Integer> tricksWon = new ArrayList<>(Arrays.asList(0, 0));
        trickNumber = 1;
        for (int i = 0; i < 10; i++) {
            //Prepare for trick
            trick = new ArrayList<>();
            gameController.updateTrick(trick, new ArrayList<>());
            gameController.updateRoundInfo(players, playerIndex, tricksWon, trickNumber);

            //Play trick and display results
            int winningTeam = playTrick(in, out);
            tricksWon.set(winningTeam, tricksWon.get(winningTeam) + 1);
            gameController.updateRoundInfo(players, playerIndex, tricksWon, trickNumber);

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

    private int playTrick(DataInputStream in, DataOutputStream out) throws IOException, InterruptedException {
        List<Player> trickPlayers = new ArrayList<>();
        int playerTurn = receiveInt(in);
        while (playerTurn != -1) {
            gameController.setPlayerTurn(players, playerTurn == playerIndex, playerTurn);
            if (playerTurn == playerIndex) {
                playing = true;
                while (playing) {
                    Thread.sleep(100);
                }
                out.writeUTF(cardPlayed.toString());
            }

            Card played = new Card(receiveString(in));
            trick.add(played);
            trickPlayers.add(players.get(playerTurn));
            gameController.updateTrick(trick, trickPlayers);
            if (playerTurn == playerIndex) {
                gameController.updateHand(players.get(playerIndex).hand);
            }
            playerTurn = receiveInt(in);
        }
        Player winner = players.get(receiveInt(in));
        gameController.setTrickResults(winner);

        return (players.indexOf(winner) % 2);
    }

    @Override
    void afterConnection() throws IOException, InterruptedException {
        gameController.updateHand(new ArrayList<>());
        DataInputStream in = clientSockets.get(0).in;
        DataOutputStream out = clientSockets.get(0).out;

        out.writeUTF(name);
        Thread.sleep(250);
        receiveBool(in);
        gameController.setRoundInfoLabelText("Waiting the host to choose teams...");
        playerIndex = receiveInt(in);

        int numPlayers = receiveInt(in);
        players = new ArrayList<>();
        for (int i = 0; i < numPlayers; i++) {
            players.add(new Player(receiveString(in)));
        }
        gameController.setPlayerList(players);

        initializeGame(in);
        while (initializeRound(in)) {
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
