package onenight.playableClasses;

import onenight.playableClasses.Actions.ActionVisitor;

public class Villager extends PlayableClass{
    @Override
    public void accept(ActionVisitor v) {
        v.doAction(this);
    }
}
