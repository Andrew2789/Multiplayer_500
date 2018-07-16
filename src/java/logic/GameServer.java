package Java.Logic;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameServer extends SocketThread {
    private Game game;
    private boolean teamsSet = false;

    public GameServer(int port, int players, Runnable onFail, Runnable onSuccess, Runnable onServerConnected, Runnable onDisconnect) {
        super(port, players, onFail, onSuccess, onServerConnected, onDisconnect);
    }


    public void sendGame(DataOutputStream out, Game game, int playerIndex) {

    }

    public void setTeams(List<Player> team1, List<Player> team2) {
        List<ClientSocket> newClientSockets = new ArrayList<>();
        List<Player> newPlayers = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            newPlayers.add(team1.get(i));
            newPlayers.add(team2.get(i));
        }
        for (int i = 0; i < 4; i++) {
            int oldIndex = game.players.indexOf(newPlayers.get(i));
            newClientSockets.add(clientSockets.get(oldIndex));
        }
        game.players = newPlayers;
        clientSockets = newClientSockets;
        teamsSet = true;
    }

    @Override
    void afterConnection() throws IOException, InterruptedException {
        System.out.println("Server started");
        game = new Game();
        while (game.players.size() < 4) {
            acceptConnection();
            if (exit) {
                return;
            }

            //Ensure name is not duplicate
            String name = receiveString(clientSockets.get(connections - 1).in);
            boolean duplicateFound = false;
            for (Player player : game.players) {
                if (player.getName().equals(name)) {
                    duplicateFound = true;
                    name += " ";
                    break;
                }
            }
            while (duplicateFound) {
                duplicateFound = false;
                name += "I";
                for (Player player : game.players) {
                    if (player.getName().equals(name)) {
                        duplicateFound = true;
                        break;
                    }
                }
            }
            game.players.add(new Player(name));
            Main.gameSetupController.addPlayer(game.players.get(game.players.size() - 1));
        }

        for (ClientSocket clientSocket: clientSockets) {
            //Notify players that the host is choosing teams
            clientSocket.out.writeBoolean(true);
        }

        //Wait for the user to select teams
        while (!teamsSet) {
            Thread.sleep(100);
            if (exit) {
                return;
            }
        }

        //Send player index, number of players, and each player's name
        for (int i = 0; i < clientSockets.size(); i++) {
            clientSockets.get(i).out.writeInt(i);
            clientSockets.get(i).out.writeInt(clientSockets.size());
            for (Player player: game.players) {
                clientSockets.get(i).out.writeUTF(player.getName());
            }
        }

        playRound();
    }

    private void playRound() throws IOException {
        game.deal();
        for (int i = 0; i < game.players.size(); i++) {
            clientSockets.get(i).out.writeBoolean(true);
            sendCardList(clientSockets.get(i).out, game.players.get(i).hand);
        }

        game.bid = new Bid(8, 'd');
        for (ClientSocket clientSocket: clientSockets) {
            clientSocket.out.writeUTF(game.bid.toString());
        }

        for (int i = 0; i < 10; i++) {
            playTrick();
        }
    }

    private void playTrick() throws IOException {
        int startingPlayer = 2, cardsPlayed = 0;
        game.trick = new ArrayList<>();
        game.trickPlayers = new ArrayList<>();
        Character leadingSuit = null;
        do {
            int currentPlayer = (startingPlayer + cardsPlayed) % game.players.size();
            for (ClientSocket clientSocket: clientSockets) {
                clientSocket.out.writeInt(currentPlayer);
            }
            Card played = new Card(receiveString(clientSockets.get(currentPlayer).in));
            if (leadingSuit == null) {
                leadingSuit = played.getSuit();
            }
            for (ClientSocket clientSocket: clientSockets) {
                clientSocket.out.writeUTF(played.toString());
            }
            game.trick.add(played);
            game.trickPlayers.add(game.players.get(currentPlayer));
            cardsPlayed++;
        } while (cardsPlayed < game.players.size());

        int winnerIndex = game.players.indexOf(game.findWinner(leadingSuit));
        //Signal that the trick is over, and send the winner to all players
        for (ClientSocket clientSocket: clientSockets) {
            clientSocket.out.writeInt(-1);
            clientSocket.out.writeInt(winnerIndex);
        }
        //Wait for all players to click continue
        for (ClientSocket clientSocket: clientSockets) {
            receiveBool(clientSocket.in);
        }
    }


    private void sendCardList(DataOutputStream out, List<Card> cards) throws IOException {
        out.writeInt(cards.size());
        for(Card card: cards) {
            out.writeUTF(card.toString());
        }
    }
}
