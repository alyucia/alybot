package discordFrontend;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class runBot {
    public static void main(String[] args) throws IOException {
        Properties prop = new Properties();
        FileInputStream ip = new FileInputStream("src/main/java/discordFrontend/config/botconfig.properties");
        prop.load(ip);
        String token = prop.getProperty("token");
        DiscordBot discordBot = new DiscordBot(token);
        discordBot.startListeners();
    }
}
