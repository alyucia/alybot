package onenight;

import onenight.gameState.GameState;

import java.util.Map;

public class Game {
    GameState currentState;
    Map<String, Player[]> playerMap;
    String unusedRoles;
    public Game (Map<String, Player[]> players){
        playerMap = players;
    }
}
