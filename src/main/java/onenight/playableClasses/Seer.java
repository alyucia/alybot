package onenight.playableClasses;

import onenight.playableClasses.Actions.ActionVisitor;

public class Seer extends PlayableClass{
    @Override
    public void accept(ActionVisitor v) {
        v.doAction(this);
    }
}
