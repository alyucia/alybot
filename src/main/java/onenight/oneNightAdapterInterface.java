package onenight;


import java.util.ArrayList;

public interface oneNightAdapterInterface {
    public void postMessageToChannel(String msg);
    public ArrayList<Integer> getUserSelection(String playerID, String msg);
    public void gameEnd(Boolean townWin);
}
