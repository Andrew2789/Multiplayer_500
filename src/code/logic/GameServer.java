package code.logic;

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
        //Reorder sockets
        List<ClientSocket> newClientSockets = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            newClientSockets.add(clientSockets.get(game.getPlayers().indexOf(team1.get(i))));
            newClientSockets.add(clientSockets.get(game.getPlayers().indexOf(team2.get(i))));
        }
        clientSockets = newClientSockets;

        //Reorder players
        List<Integer> teams = new ArrayList<>();
        for (Player player: game.getPlayers()) {
            if (team1.contains(player)) {
                teams.add(1);
            } else {
                teams.add(2);
            }
        }
        game.setTeams(teams);
        teamsSet = true;
    }

    @Override
    void afterConnection() throws IOException, InterruptedException {
        System.out.println("Server started");
        List<Player> players = new ArrayList<>();
        while (players.size() < 4) {
            acceptConnection();
            if (exit) {
                return;
            }

            //Ensure name is not duplicate
            String name = receiveString(clientSockets.get(connections - 1).in);
            boolean duplicateFound = false;
            for (Player player : players) {
                if (player.getName().equals(name)) {
                    duplicateFound = true;
                    name += " ";
                    break;
                }
            }
            while (duplicateFound) {
                duplicateFound = false;
                name += "I";
                for (Player player : players) {
                    if (player.getName().equals(name)) {
                        duplicateFound = true;
                        break;
                    }
                }
            }
            players.add(new Player(name));
            Main.gameSetupController.addPlayer(players.get(players.size() - 1));
        }

        for (ClientSocket clientSocket: clientSockets) {
            //Notify players that the host is choosing teams
            clientSocket.out.writeBoolean(true);
        }
        game = new Game(players);

        //Wait for the user to select teams
        while (!teamsSet) {
            sleep(100);
            if (exit) {
                return;
            }
        }

        //Send player index, number of players, and each player's name
        for (int i = 0; i < clientSockets.size(); i++) {
            clientSockets.get(i).out.writeInt(i);
            clientSockets.get(i).out.writeInt(clientSockets.size());
            for (Player player: game.getPlayers()) {
                clientSockets.get(i).out.writeUTF(player.getName());
            }
        }

        while (true) {
            playRound();
        }
    }

    private void playRound() throws IOException {
        game.deal();
        for (int i = 0; i < game.getPlayers().size(); i++) {
            clientSockets.get(i).out.writeBoolean(true);
            sendCardList(clientSockets.get(i).out, game.getPlayer(i).getHand());
        }

        game.setBid(new Bid(8, 'd'), 2);
        for (ClientSocket clientSocket: clientSockets) {
            clientSocket.out.writeUTF(game.getBid().toString());
        }

        int winner = 0;
        for (int i = 0; i < 10; i++) {
            if (i == 0) {
                winner = playTrick(game.getBidWinner());
            } else {
                winner = playTrick(winner);
            }
        }
        game.wasBidSuccessful();
        //Wait for all players to click continue
        for (ClientSocket clientSocket: clientSockets) {
            receiveBool(clientSocket.in);
        }
    }

    private int playTrick(int startingPlayer) throws IOException {
        int cardsPlayed = 0;
        Character leadingSuit = null;
        do {
            int currentPlayer = (startingPlayer + cardsPlayed) % game.getPlayers().size();
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
            game.cardPlayed(played, currentPlayer, true);
            cardsPlayed++;
        } while (cardsPlayed < game.getPlayers().size());

        int winnerIndex = game.getPlayers().indexOf(game.findTrickWinner());
        //Signal that the trick is over, and send the winner to all players
        for (ClientSocket clientSocket: clientSockets) {
            clientSocket.out.writeInt(-1);
            clientSocket.out.writeInt(winnerIndex);
        }
        //Wait for all players to click continue
        for (ClientSocket clientSocket: clientSockets) {
            receiveBool(clientSocket.in);
        }

        return winnerIndex;
    }

    private void sendCardList(DataOutputStream out, List<Card> cards) throws IOException {
        out.writeInt(cards.size());
        for(Card card: cards) {
            out.writeUTF(card.toString());
        }
    }
}
