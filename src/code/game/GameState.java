package code.game;

public enum GameState {
    SETUP(0), TEAM_SELECTION(1), BIDDING(2), PICKING(3), PLAYING_TRICK(4), REVIEWING_TRICK(5), REVIEWING_GAME(6);

    private int id;

    GameState(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static GameState fromId(int id) {
        for (GameState gameState : GameState.values()) {
            if (gameState.id == id) {
                return gameState;
            }
        }
        return null;
    }
}
