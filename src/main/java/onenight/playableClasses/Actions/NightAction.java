package onenight.playableClasses.Actions;

import onenight.Game;
import onenight.errors.badInputError;
import onenight.oneNightAdapterInterface;
import onenight.playableClasses.*;

public class NightAction implements ActionVisitor {
    int[] userInput;
    public NightAction(int[] ui){
        this.userInput = ui;
    }
    @Override
    public void doAction(PlayableClassInterface player) throws badInputError {
        player.accept(this);
    }

    @Override
    public void doAction(Doppelganger player) {

    }

    @Override
    public void doAction(Drunk player) {

    }

    @Override
    public void doAction(Hunter player) {

    }

    @Override
    public void doAction(Insomniac player) {

    }

    @Override
    public void doAction(Masons player) {

    }

    @Override
    public void doAction(Minion player) {

    }

    @Override
    public void doAction(Robber player) {

    }

    @Override
    public void doAction(Seer player) {

    }

    @Override
    public void doAction(Tanner player) {
        //no action
    }

    @Override
    public void doAction(Troublemaker player) {

    }

    @Override
    public void doAction(Villager player) {
        //no action
    }

    @Override
    public void doAction(Werewolf player) {

    }
}
