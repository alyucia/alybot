package discordFrontend;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class runBot {


    private static final String BOT_CONFIG_FILE_PATH = "src/main/resources/botconfig.properties";
    private static final String AUTOREACT_FILE_PATH = "src/main/resources/autoreacts.ser";

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Properties prop = new Properties();
        FileInputStream ip = new FileInputStream(BOT_CONFIG_FILE_PATH);
        HashMap reactmap = new HashMap();
        try {
            FileInputStream reacts = new FileInputStream(AUTOREACT_FILE_PATH);
            ObjectInputStream reactsois = new ObjectInputStream(reacts);
            reactmap = (HashMap) reactsois.readObject();
            reacts.close();
            reactsois.close();
        } catch (FileNotFoundException e){

        }
        prop.load(ip);
        String token = prop.getProperty("token");
        String prefix = prop.getProperty("prefix");
        DiscordBot discordBot = new DiscordBot(token, prefix, reactmap);
        discordBot.startListener();
        ip.close();
    }
}
