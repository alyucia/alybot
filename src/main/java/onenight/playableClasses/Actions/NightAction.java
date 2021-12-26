package onenight.playableClasses.Actions;

import onenight.playableClasses.*;

public class NightAction implements ActionVisitor {
    @Override
    public void doAction(PlayableClass player) {
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
