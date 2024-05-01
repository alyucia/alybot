package discordFrontend;


import com.vdurmont.emoji.EmojiParser;
import discordFrontend.dndscraper.DNDScraper;
import org.javacord.api.interaction.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import sheetWriter.SheetWriter;
import org.javacord.api.*;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;


public class DiscordBot {
    private final DiscordApi api;
    private final ExecutorService executors;
    private final String prefix;

    public DiscordBot(String botToken, String px){
        this.api = new DiscordApiBuilder().setToken(botToken).login().join();
        this.executors = api.getThreadPool().getExecutorService();
        this.prefix = px;

        System.out.println("You can invite the bot by using the following url: " + api.createBotInvite());
    }
    public void startListener() {

        api.addMessageCreateListener(event -> {
            Pattern pattern = Pattern.compile("^" + prefix + "([a-zA-Z]*)(?:\\s(.*))?", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(event.getMessageContent());
            if (matcher.matches()) {
                String command = matcher.group(1);
                String parameters = matcher.group(2);
                executors.execute(()->{
                    try {
                        doCommand(command, parameters, event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });

        //slash command examples

        SlashCommand ping = SlashCommand.with("ping", "Checks the functionality of this command")
                .createGlobal(api)
                .join();
        SlashCommand timestamp = SlashCommand.with("timestamp", "A command dedicated to channels",
                    Arrays.asList(
                            SlashCommandOption.create(SlashCommandOptionType.STRING, "time", "HH:MM", false),
                            SlashCommandOption.create(SlashCommandOptionType.STRING, "timezone", "ZZZ", false),
                            SlashCommandOption.create(SlashCommandOptionType.STRING, "month", "Month", false),
                            SlashCommandOption.create(SlashCommandOptionType.STRING, "date", "DD", false),
                            SlashCommandOption.create(SlashCommandOptionType.STRING, "year", "YYYY", false)

                    )
                )
                .createGlobal(api)
                .join();

        api.addSlashCommandCreateListener(event -> {
            SlashCommandInteraction slashCommandInteraction = event.getSlashCommandInteraction();
            String command = slashCommandInteraction.getCommandName();
            String response;
            try {
                response = switch (command) {
                    case "timestamp" -> timeStampSlash(slashCommandInteraction);
                    case "ping" -> "ping!";
                    default -> "null";
                };
            } catch (Exception e){
                response = "Error";
            }
            slashCommandInteraction.createImmediateResponder()
                    .setContent(response)
                    .respond();

        });
    }
    public void doCommand(String command, String parameters, MessageCreateEvent event) throws exceptions.NotCommandException, exceptions.BadParameterException, ExecutionException, InterruptedException {
        switch (command){
            case "ping":
                ping(event);
                break;
            case "startsession":
                startDND(event);
                break;
            case "startgame":
                startGame(event);
                break;
            case "clearmsgs":
                clearMessages(event, parameters);
                break;
            case "timestamp":
                timeStamp(event, parameters);
                break;
            default:
                throw new exceptions.NotCommandException();
        }
    }



    //Starts a DND tracking session.
    private void startDND(MessageCreateEvent event) {
        Message userMessage = event.getMessage();
        TextChannel dndChannel = event.getChannel();
        userMessage.addReaction(EmojiParser.parseToUnicode(":white_check_mark:"));

        SheetWriter sheetWriter = new SheetWriter("1UQXrcdjCrAHDvJUgGk0rL5C7Tg9S9PTzz3Q3udovl3E");

        DNDScraper dndScraper = new DNDScraper(api.getThreadPool(), dndChannel, sheetWriter, String.valueOf(api.getClientId()));
        dndScraper.startListening();

        //Example sheet write
/*        try {
            double startTime = System.nanoTime();
            sheetWriter.writeInfo(List.of(new String[]{"test1", "test2", "testing3"}));
            double endTime = System.nanoTime();
            System.out.println((endTime - startTime) / 1000000);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }*/
    }




    private void startGame(MessageCreateEvent event) throws ExecutionException, InterruptedException {
        List<String> ids = new ArrayList();
        Message userMessage = event.getMessage();
        TextChannel gameChannel = event.getChannel();
        Message starterMessage = gameChannel.sendMessage("Starting game!").get();
        starterMessage.addReaction(EmojiParser.parseToUnicode(":white_check_mark:"));
        sleep(15000);
        List<User> userList= starterMessage.getReactionByEmoji(EmojiParser.parseToUnicode(":white_check_mark:")).get().getUsers().get();
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
    private void ping(MessageCreateEvent event){
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

    private void clearMessages(MessageCreateEvent event, String parameters) throws ExecutionException, InterruptedException, exceptions.BadParameterException {
        if (event.getMessageAuthor().getId() != api.getOwnerId()){
            event.getMessage().addReaction(EmojiParser.parseToUnicode(":x:"));
            return;
        }
        try {
            int num = Integer.parseInt(parameters) + 1;
            TextChannel clearChannel = event.getChannel();
            MessageSet msgs = clearChannel.getMessages(num).get();
            msgs.deleteAll();
        } catch (NumberFormatException e){
            throw new exceptions.BadParameterException();
        }

    }
    private void timeStamp(MessageCreateEvent event, String parameters) throws ExecutionException, InterruptedException, exceptions.BadParameterException {
        TextChannel channel = event.getChannel();
        try {
            DateTime dt = helpers.parseInput(parameters);
            long timestamp = (long) Math.floor(dt.getMillis() / 1000);
            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z");
            String dtStr = fmt.print(dt);
            channel.sendMessage(dtStr + "\n" +
                    "<t:" + timestamp + ">\n"
                    + timestamp);
        } catch (Exception e){
            channel.sendMessage("Bad format.");
            throw new exceptions.BadParameterException();
        }

    }

    private String timeStampSlash(SlashCommandInteraction event) throws exceptions.BadParameterException {
        DateTime dt = new DateTime();
        List<SlashCommandInteractionOption> options = event.getOptions();
        for (SlashCommandInteractionOption op : options){
            if(op.getStringValue().isEmpty())
                break;
            String value = op.getStringValue().get();
            switch (op.getName()){
                case "time":
                    String[] vals = value.split(":");
                    dt.hourOfDay().setCopy(Integer.parseInt(vals[0]));
                    dt.minuteOfHour().setCopy(Integer.parseInt(vals[1]));
                    break;
                case "timezone":
                    dt = dt.withZone(DateTimeZone.forTimeZone(TimeZone.getTimeZone(value)));
                    break;
                case "month":
                    dt = dt.monthOfYear().setCopy(value);
                    break;
                case "date":
                    dt = dt.dayOfMonth().setCopy(Integer.parseInt(value));
                    break;
                case "year":
                    dt = dt.year().setCopy(Integer.parseInt(value));
                    break;
                default:
                    break;

            }
        }
        String res;
        try {
            long timestamp = (long) Math.floor(dt.getMillis() / 1000);
            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z");
            String dtStr = fmt.print(dt);
            res = dtStr + "\n" +
                    "<t:" + timestamp + ">\n"
                    + timestamp;
        } catch (Exception e){
            throw new exceptions.BadParameterException();
        }
        return res;
    }
}
