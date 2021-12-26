package onenight.playableClasses.Actions;

import onenight.playableClasses.*;

public interface ActionVisitor {
    public void doAction(PlayableClass player);

    public void doAction(Doppelganger player);
    public void doAction(Drunk player);
    public void doAction(Hunter player);
    public void doAction(Insomniac player);
    public void doAction(Masons player);
    public void doAction(Minion player);
    public void doAction(Robber player);
    public void doAction(Seer player);
    public void doAction(Tanner player);
    public void doAction(Troublemaker player);
    public void doAction(Villager player);
    public void doAction(Werewolf player);
}
