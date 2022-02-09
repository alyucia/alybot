package onenight;

import frontend.discordOnenightAdapter;
import onenight.gameState.GameState;

import java.util.Map;

public class Game {
    oneNightAdapterInterface frontend;
    GameState currentState;
    Map<String, Player[]> playerMap;
    String unusedRoles;
    public Game (Map<String, Player[]> players){
        playerMap = players;
        //frontend = new discordOnenightAdapter();
    }
}
