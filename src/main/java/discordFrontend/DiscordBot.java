package discordFrontend;


import discordFrontend.dndscraper.DNDScraper;
import sheetWriter.SheetWriter;
import org.javacord.api.*;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;

public class DiscordBot {
    private final DiscordApi api;

    public DiscordBot(String botToken){
        this.api = new DiscordApiBuilder().setToken(botToken).login().join();

        System.out.println("You can invite the bot by using the following url: " + api.createBotInvite());
    }
    public void startListeners() {
        api.addMessageCreateListener(event -> {
            Pattern pattern = Pattern.compile("^!ping", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(event.getMessageContent());
            if (matcher.find()) {
                User author = event.getMessageAuthor().asUser().get();
                String id = author.getIdAsString();
                event.getChannel().sendMessage("Pong! <@" + id + ">");
/*                try {
                    PrivateChannel userChannel = author.openPrivateChannel().get();
                    userChannel.sendMessage("Test!");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }*/
            }
        });
        api.addMessageCreateListener(event -> {
            Pattern pattern = Pattern.compile("^!clearmsgs", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(event.getMessageContent());
            if (matcher.find()) {
                if (event.getMessageAuthor().getId() != api.getOwnerId()){
                    event.getMessage().addReaction("\u274C");
                    return;
                }
                int num = Integer.parseInt(event.getMessageContent().split(" ")[1]) + 1;
                TextChannel clearChannel = event.getChannel();
                try {
                    MessageSet msgs = clearChannel.getMessages(num).get();
                    msgs.deleteAll();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        api.addMessageCreateListener(event -> {
            Pattern pattern = Pattern.compile("^!startgame", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(event.getMessageContent());
            if (matcher.find()) {
                Thread gameThread = new Thread(() -> {
                    try {
                        startGame(event);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                gameThread.start();
            }
        });

        api.addMessageCreateListener(event -> {
            Pattern pattern = Pattern.compile("^!startsession", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(event.getMessageContent());
            if (matcher.find()) {
                Thread dndThread = new Thread(() -> {
                    try {
                        startDND(event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                dndThread.start();
            }
        });

        //slash command examples
/*        SlashCommand ping = SlashCommand.with("ping", "test").createGlobal(api).join();
        api.addSlashCommandCreateListener(event -> {
            SlashCommandInteraction slashCommandInteraction = event.getSlashCommandInteraction();    slashCommandInteraction.createImmediateResponder()
                    .setContent("Test")
                    .respond();

        });*/
    }

    //Starts a DND tracking session.
    private void startDND(MessageCreateEvent event) throws ExecutionException, InterruptedException {
        Message userMessage = event.getMessage();
        TextChannel dndChannel = event.getChannel();
        userMessage.addReaction("\u2705");

        SheetWriter sheetWriter = new SheetWriter("1eftAShN3ANHruHbOoeccwpo3gdEtz8LiLygOtO6Q3I4");

        DNDScraper dndScraper = new DNDScraper(dndChannel, sheetWriter);
        dndScraper.startListening();
        //Example sheet write
/*        try {
            sheetWriter.writeInfo(List.of(new String[]{"test1", "test2", "testing3"}));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }*/
    }




    private void startGame(MessageCreateEvent event) throws ExecutionException, InterruptedException {
        List<String> ids = new ArrayList();
        Message userMessage = event.getMessage();
        TextChannel gameChannel = event.getChannel();
        Message starterMessage = gameChannel.sendMessage("Starting game!").get();
        starterMessage.addReaction("\u2705");
        sleep(15000);
        List<User> userList= starterMessage.getReactionByEmoji("\u2705").get().getUsers().get();
        for (User u : userList) {
            ids.add(u.getIdAsString());
        }
        gameChannel.sendMessage("Starting game with " + ids);


        //Add user to list if they respond to the react
/*        starterMessage.addReactionAddListener(reactEvent ->{
           if (reactEvent.getEmoji().equalsEmoji("\u2705")) {
               User reactUser = reactEvent.getMessageAuthor().get().asUser().get();
               String userId = reactUser.getIdAsString();
               if (!ids.contains(userId))
                   ids.add(userId);
               System.out.println(ids);
           }
        });


        starterMessage.addReactionRemoveListener(reactEvent ->{
            System.out.println(reactEvent.getEmoji());
            if (reactEvent.getEmoji().equalsEmoji("✅")) {
                User reactUser = reactEvent.getMessageAuthor().get().asUser().get();
                String userId = reactUser.getIdAsString();
                ids.remove(userId);
                System.out.println(ids);
            }
        });*/

    }
}
