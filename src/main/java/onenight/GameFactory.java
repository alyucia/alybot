package onenight;

import onenight.playableClasses.*;

import java.util.*;

//roles [1:]
public class GameFactory {
    private int numRoles = 12;

    public GameFactory(){
    }
    public Game makeGame(Double[] playersID, int[] roles){
        Player[] players = makePlayers(playersID, roles);
        return null;
    }

    private Player[] makePlayers(Double[] ids, int[] roles) {
        Player[] players = new Player[ids.length + 3];
        int counter = 0;
        for (Double id: ids) {
            int randomIndex = (int)Math.floor(Math.random() * numRoles);
            while(roles[randomIndex] <= 0)
                randomIndex = (int)Math.floor(Math.random() * numRoles);
            roles[randomIndex]--;
            Player newPlayer = new Player(id, getPlayerClass(randomIndex));
            players[counter++] = newPlayer;
        }
        for (int i = 0; i < roles.length; i++) {
            while (roles[i] > 0) {
                players[counter++] = new Player(0, getPlayerClass(i));
            }
        }
        return players;

//        int counter = 0;
//        List<Double> idlist = Arrays.asList(ids);
//        for (int i = 0; i < roles.length; i++) {
//            int c = roles[i];
//            for (int j = 0; i < c; j++) {
//                int randomIndex = (int)Math.floor(Math.random() * idlist.size());
//                Double id = idlist.remove(randomIndex);
//                Player newPlayer = new Player((double)id, getPlayerClass(i));
//                players[counter] = newPlayer;
//                counter++;
//            }
//        }

    }

    private PlayableClassInterface getPlayerClass(int i) {
        PlayableClassInterface playerClass;
        switch (i) {
            case 0: playerClass = new Doppelganger();
                break;
            case 1: playerClass = new Werewolf();
                break;
            case 2: playerClass = new Minion();
                break;
            case 3: playerClass = new Masons();
                break;
            case 4: playerClass = new Seer();
                break;
            case 5: playerClass = new Robber();
                break;
            case 6: playerClass = new Troublemaker();
                break;
            case 7: playerClass = new Drunk();
                break;
            case 8: playerClass = new Insomniac();
                break;
            case 9: playerClass = new Villager();
                break;
            case 10: playerClass = new Hunter();
                break;
            case 11: playerClass = new Tanner();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + i);
        }
        return playerClass;
    }


}
