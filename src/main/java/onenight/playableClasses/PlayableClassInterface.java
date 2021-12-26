package onenight.playableClasses;

import onenight.playableClasses.Actions.ActionVisitor;

public interface PlayableClassInterface {
    public void accept(ActionVisitor v);
}
