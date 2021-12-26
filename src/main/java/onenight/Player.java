package onenight;

import onenight.playableClasses.PlayableClassInterface;

public class Player {

    private double playerID;
    private PlayableClassInterface playerClass;
    private String playerCurrentClass;

    public Player(double id, PlayableClassInterface pClass){
        this.playerID = id;
        this.playerClass = pClass;
        this.playerCurrentClass = pClass.getClass().getSimpleName();
    }

    public String getPlayerCurrentClass() {
        return playerCurrentClass;
    }

    public void setPlayerCurrentClass(String newClass){
        this.playerCurrentClass = newClass;
    }
}
