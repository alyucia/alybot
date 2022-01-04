package discordFrontend;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class runBot {


    private static final String BOT_CONFIG_FILE_PATH = "src/main/resources/botconfig.properties";

    public static void main(String[] args) throws IOException {
        Properties prop = new Properties();
        FileInputStream ip = new FileInputStream(BOT_CONFIG_FILE_PATH);
        prop.load(ip);
        String token = prop.getProperty("token");
        DiscordBot discordBot = new DiscordBot(token);
        discordBot.startListeners();
    }
}
