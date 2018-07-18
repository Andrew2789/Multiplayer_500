package code.network;

import code.game.Bid;
import code.game.Card;
import code.game.Game;
import code.game.Player;
import code.gui.GameController;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameClient extends SocketThread {
    private GameController gameController;
    private ChatClient chatClient;
    private String name;
    private Card cardPlayed = null;
    private boolean playing, readyToContinue;

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
        if (playing) {
            gameController.updateHand(game.getPlayer(playerIndex).getHand(), game.getBid().filterPlayable(game.getPlayer(playerIndex).getHand(), game.getTrumpsPlayed(), game.getLeadingSuit()));
        } else {
            gameController.updateHand(game.getPlayer(playerIndex).getHand(), null);
        }
    }

    public void submitChatMessage(String message) {
        chatClient.sendMessage(message);
    }

    public void readyToContinue() {
        readyToContinue = true;
    }

    private void waitForContinue() throws InterruptedException {
        readyToContinue = false;
        while (!readyToContinue) {
            sleep(100);
            if (exit) {
                return;
            }
        }
    }

    @Override
    void afterConnection() throws IOException, InterruptedException {
        gameController.updateHand(new ArrayList<>(), null);
        DataInputStream in = clientSockets.get(0).in;
        DataOutputStream out = clientSockets.get(0).out;

        out.writeBoolean(true); //register as a game socket (not chat)

        out.writeUTF(name);
        name = receiveString(in);
        chatClient = new ChatClient(name, ipAddress, port, () -> System.out.println("Chat failed to connect."), () -> System.out.println("Chat connected."), () -> {}, gameController);
        Main.setChatClient(chatClient);
        chatClient.start();
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

    private boolean initializeRound(DataInputStream in) throws IOException {
        if (!receiveBool(in)) {
            return false; //Game over
        }
        game.getPlayer(playerIndex).setHand(receiveCardList(in));
        gameController.updateHand(game.getPlayer(playerIndex).getHand(), null);
        game.hostDealt();

        return true;
    }

    private void bid(DataInputStream in, DataOutputStream out) throws IOException, InterruptedException {
        game.setBid(new Bid(receiveString(in)), 2);
    }

    private void playRound(DataInputStream in, DataOutputStream out) throws IOException, InterruptedException {
        int trickNumber = 1;
        for (int i = 0; i < 10; i++) {
            //Prepare for trick
            gameController.updateTrick(game.getTrick(), game.getTrickPlayers());
            gameController.updateRoundInfo(game.getPlayers(), playerIndex, game.getTricksWon(), trickNumber, game.getRoundNumber());

            //Play trick and display results
            playTrick(in, out);
            gameController.updateRoundInfo(game.getPlayers(), playerIndex, game.getTricksWon(), trickNumber, game.getRoundNumber());

            //Wait for player to click continue
            waitForContinue();
            out.writeBoolean(true);
            trickNumber++;
        }
        boolean won = game.wasBidSuccessful();
        gameController.updateRoundInfo(game.getPlayers(), playerIndex, game.getTricksWon(), trickNumber, game.getRoundNumber());
        gameController.setRoundResults(game.getBid(), won, game.getBidWinner(), game.getTrickPoints(), game.getPlayers());
        waitForContinue();
        out.writeBoolean(true);
    }

    private void playTrick(DataInputStream in, DataOutputStream out) throws IOException, InterruptedException {
        int playerTurn = receiveInt(in);
        while (playerTurn != -1) {
            playing = false;
            if (playerTurn == playerIndex) {
                gameController.setPlayerTurn(game.getPlayers(),
                        game.getBid().filterPlayable(game.getPlayer(playerTurn).getHand(), game.getTrumpsPlayed(), game.getLeadingSuit()),
                        playerIndex, playerTurn);
                playing = true;
                while (playing) {
                    sleep(100);
                }
                out.writeUTF(cardPlayed.toString());
                playing = true;
            } else {
                gameController.setPlayerTurn(game.getPlayers(), null, playerIndex, playerTurn);
            }

            Card played = new Card(receiveString(in));
            game.cardPlayed(played, playerTurn, playing);
            gameController.updateTrick(game.getTrick(), game.getTrickPlayers());
            if (playerTurn == playerIndex) {
                gameController.updateHand(game.getPlayer(playerIndex).getHand(), null);
            }
            playerTurn = receiveInt(in);
        }
        game.findTrickWinner();
        Player winner = game.getPlayer(receiveInt(in));
        gameController.setTrickResults(winner);
        gameController.setPlayerTurn(game.getPlayers(), null, playerIndex, -1);
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
