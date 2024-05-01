package discordFrontend;


import com.vdurmont.emoji.EmojiParser;
import discordFrontend.dndscraper.DNDScraper;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.*;
import org.javacord.api.interaction.callback.ComponentInteractionOriginalMessageUpdater;
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

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;


public class DiscordBot {
    private final DiscordApi api;
    private final ExecutorService executors;
    private final String prefix;
    private HashMap<Long, HashMap<String, AutoReact>> autoreacts;

    public DiscordBot(String botToken, String px, HashMap map){
        this.api = new DiscordApiBuilder().setToken(botToken).login().join();
        this.executors = api.getThreadPool().getExecutorService();
        this.prefix = px;
        this.autoreacts = map;

        System.out.println("You can invite the bot by using the following url: " + api.createBotInvite());
    }
    public void startListener() {

        api.addMessageCreateListener(event -> {
            Long server = event.getServer().get().getId();
            HashMap<String, AutoReact> ars = autoreacts.getOrDefault(server, new HashMap<String, AutoReact>());
            if(ars.containsKey(event.getMessageContent())){
                ars.get(event.getMessageContent()).trigger(event.getChannel(), event.getMessage().getUserAuthor().get().getId());
            }

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
        SlashCommand timestamp = SlashCommand.with("timestamp", "Create dynamic timestamps",
                        Arrays.asList(
                                SlashCommandOption.create(SlashCommandOptionType.STRING, "time", "HH:MM - 24 Hour time", false),
                                SlashCommandOption.create(SlashCommandOptionType.STRING, "timezone", "ZZZ", false),
                                SlashCommandOption.create(SlashCommandOptionType.STRING, "month", "Month", false),
                                SlashCommandOption.create(SlashCommandOptionType.STRING, "date", "DD", false),
                                SlashCommandOption.create(SlashCommandOptionType.STRING, "year", "YYYY", false)
                        )
                )
                .createGlobal(api)
                .join();
        SlashCommand createAutoReact = SlashCommand.with("autoreact", "Commands to modify autoreact messages",
                        Arrays.asList(
                                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "create", "Creates autoreact messages",
                                        Arrays.asList(
                                                SlashCommandOption.create(SlashCommandOptionType.STRING, "trigger", "Trigger phrase", true),
                                                SlashCommandOption.create(SlashCommandOptionType.STRING, "message", "Autoreact message", true),
                                                SlashCommandOption.create(SlashCommandOptionType.STRING, "users", "Default - all", false)
                                        )
                                )
                        )
                )
                .createGlobal(api)
                .join();
        SlashCommand reminder = SlashCommand.with("reminder", "Create reminders",
                        Arrays.asList(
                                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "timestamp", "Create reminder at the given timestamp. Default is 1 day.",
                                        Arrays.asList(
                                                SlashCommandOption.create(SlashCommandOptionType.STRING, "info", "Description of reminder", true),
                                                SlashCommandOption.create(SlashCommandOptionType.STRING, "time", "HH:MM - 24 Hour time", false),
                                                SlashCommandOption.create(SlashCommandOptionType.STRING, "timezone", "ZZZ", false),
                                                SlashCommandOption.createWithChoices(SlashCommandOptionType.STRING, "month", "Month", false,
                                                        Arrays.asList(
                                                                SlashCommandOptionChoice.create("January","January"),
                                                                SlashCommandOptionChoice.create("February","February"),
                                                                SlashCommandOptionChoice.create("March","March"),
                                                                SlashCommandOptionChoice.create("April","April"),
                                                                SlashCommandOptionChoice.create("May","May"),
                                                                SlashCommandOptionChoice.create("June","June"),
                                                                SlashCommandOptionChoice.create("July","July"),
                                                                SlashCommandOptionChoice.create("August","August"),
                                                                SlashCommandOptionChoice.create("September","September"),
                                                                SlashCommandOptionChoice.create("October","October"),
                                                                SlashCommandOptionChoice.create("November","November"),
                                                                SlashCommandOptionChoice.create("December","December")
                                                        )),
                                                SlashCommandOption.create(SlashCommandOptionType.INTEGER, "date", "DD", false),
                                                SlashCommandOption.create(SlashCommandOptionType.INTEGER, "year", "YYYY", false)
                                        )
                                ),
                                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "timer", "Create timed reminder. Default is 1 hour",
                                        Arrays.asList(
                                                SlashCommandOption.create(SlashCommandOptionType.STRING, "info", "Description of reminder", true),
                                                SlashCommandOption.create(SlashCommandOptionType.INTEGER, "years", "Number of years", false),
                                                SlashCommandOption.create(SlashCommandOptionType.INTEGER, "months", "Number of months", false),
                                                SlashCommandOption.create(SlashCommandOptionType.INTEGER, "days", "Number of days", false),
                                                SlashCommandOption.create(SlashCommandOptionType.INTEGER, "hours", "Number of hours", false),
                                                SlashCommandOption.create(SlashCommandOptionType.INTEGER, "minutes", "Number of minues", false)
                                        )
                                ),
                                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "recurring", "Create recurring reminder. Default is weekly.",
                                        Arrays.asList(
                                                SlashCommandOption.create(SlashCommandOptionType.STRING, "info", "Description of reminder", true),
                                                SlashCommandOption.createWithChoices(SlashCommandOptionType.INTEGER, "interval", "Interval (daily/", true,
                                                        Arrays.asList(
                                                                SlashCommandOptionChoice.create("Daily", 0),
                                                                SlashCommandOptionChoice.create("Weekly", 1),
                                                                SlashCommandOptionChoice.create("Monthly", 2),
                                                                SlashCommandOptionChoice.create("Yearly", 3)
                                                        )
                                                ),
                                                SlashCommandOption.create(SlashCommandOptionType.STRING, "time", "HH:MM - 24 Hour time", false),
                                                SlashCommandOption.create(SlashCommandOptionType.STRING, "timezone", "ZZZ", false),
                                                SlashCommandOption.createWithChoices(SlashCommandOptionType.STRING, "month", "Month", false,
                                                        Arrays.asList(
                                                                SlashCommandOptionChoice.create("January","January"),
                                                                SlashCommandOptionChoice.create("February","February"),
                                                                SlashCommandOptionChoice.create("March","March"),
                                                                SlashCommandOptionChoice.create("April","April"),
                                                                SlashCommandOptionChoice.create("May","May"),
                                                                SlashCommandOptionChoice.create("June","June"),
                                                                SlashCommandOptionChoice.create("July","July"),
                                                                SlashCommandOptionChoice.create("August","August"),
                                                                SlashCommandOptionChoice.create("September","September"),
                                                                SlashCommandOptionChoice.create("October","October"),
                                                                SlashCommandOptionChoice.create("November","November"),
                                                                SlashCommandOptionChoice.create("December","December")
                                                        )
                                                ),
                                                SlashCommandOption.create(SlashCommandOptionType.INTEGER, "date", "DD", false),
                                                SlashCommandOption.create(SlashCommandOptionType.INTEGER, "year", "YYYY", false)
                                        )
                                )
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
                    case "ping" -> "ping!";
                    case "timestamp" -> timeStampSlash(slashCommandInteraction);
                    case "reminder" -> reminderSlash(slashCommandInteraction);
                    case "autoreact" -> autoReact(slashCommandInteraction);
                    default -> "null";
                };
                if (command.equals("ping") || command.equals("timestamp")) {
                    slashCommandInteraction.createImmediateResponder()
                            .setContent(response)
                            .respond();
                }
                /*if (command.equals("reminder") || command.equals("a")) {
                    slashCommandInteraction.createImmediateResponder()
                            .setContent(response)
                            .addComponents(
                                    ActionRow.of(
                                            Button.success("confirm", "Confirm"),
                                            Button.danger("cancel", "Cancel")
                                    )
                            )
                            .respond();
                    api.addButtonClickListener(buttonEvent->{
                        buttonEvent.getButtonInteraction();
                    });

                }*/
            } catch (Exception e){
                response = "Error: ";
                slashCommandInteraction.createImmediateResponder()
                        .setContent(response)
                        .respond();
                e.printStackTrace();
            }


        });
    }

    private String autoReact(SlashCommandInteraction event) throws Exception {
        Server server = event.getServer().get();
        SlashCommandInteractionOption op = event.getFirstOption().get();
        String res = "";
        switch (op.getName()) {
            case "create":
                HashMap<String, AutoReact> reacts = autoreacts.getOrDefault(server.getId(), new HashMap<String, AutoReact>());
                String trig = op.getOptionByName("trigger").get().getStringValue().get();
                if (reacts.containsKey(trig))
                    throw new Exception("Already exists");
                List<Long> lids = new ArrayList<>();
                String content = op.getOptionByName("message").get().getStringValue().get();
                res = "Trigger phrase: " + trig +
                        "\nMessage content: " + content;
                if(op.getOptionByName("users").isPresent()) {
                    String users = op.getOptionByName("users").get().getStringValue().get();
                    Pattern digits = Pattern.compile("([\\d]+)");
                    Matcher matcher = digits.matcher(users);
                    while (matcher.find()) {
                        lids.add(Long.parseLong(matcher.group()));
                    }
                    res += "\nUsers:";
                    for (Long lid : lids){
                        res += " " + api.getUserById(lid).get().getName();
                    }

                }
                event.createImmediateResponder()
                        .setContent(res)
                        .addComponents(
                                ActionRow.of(
                                        Button.success("confirm", "Confirm"),
                                        Button.danger("cancel", "Cancel")
                                )
                        )
                        .respond();
                api.addButtonClickListener(buttonEvent->{
                    String type = buttonEvent.getButtonInteraction().getCustomId();
                    ComponentInteractionOriginalMessageUpdater updater = buttonEvent.getButtonInteraction().createOriginalMessageUpdater();
                    updater.setContent(buttonEvent.getButtonInteraction().getMessage().get().getContent());
                    if (type.equals("confirm")){
                        AutoReact ar = new AutoReact(lids, content);
                        reacts.put(trig, ar);
                        autoreacts.put(server.getId(), reacts);
                        helpers.writeFile(autoreacts, "autoreacts.ser");
                        updater.append("\n Successfully added");
                    } else if (type.equals("cancel")){
                        updater.append("\n Canceled");
                    }
                    updater.removeAllComponents();
                    updater.update();
                    buttonEvent.getButtonInteraction().acknowledge();

                });

                break;
        }
        return "null";
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

    private String reminderSlash(SlashCommandInteraction event) throws exceptions.BadParameterException {
        DateTime dt = new DateTime();
        String info = "";
        SlashCommandInteractionOption op = event.getFirstOption().get();
        List<SlashCommandInteractionOption> ops = op.getOptions();
        switch (op.getName()) {
            case "timestamp":
                if (ops.size() == 1){
                    dt = dt.plusDays(1);
                }
                for (SlashCommandInteractionOption ope : ops) {
                    String value = ope.getStringValue().get();
                    switch (ope.getName()) {
                        case "info":
                            info = value;
                            break;
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
                 }
            }
                break;
            case "recurring":
                for (SlashCommandInteractionOption ope : ops) {
                    String value = ope.getStringValue().get();
                    switch (ope.getName()) {
                        case "interval":
                            switch (ope.getIntValue().get()){
                                case 0:
                                    break;
                                case 1:
                                    break;
                                case 2:
                                    break;
                                case 3:
                                    break;
                            }
                            break;
                        case "info":
                            info = value;
                            break;
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
                    }
                }
                break;
            case "timer":
                if (ops.size() == 1){
                    dt = dt.plusHours(1);
                }
                for (SlashCommandInteractionOption ope : ops) {
                    System.out.println("hit");
                    String value = ope.getStringValue().get();
                    switch (ope.getName()) {
                        case "info":
                            info = value;
                            break;
                        case "years":
                            dt = dt.plusYears(Integer.parseInt(value));
                            break;
                        case "months":
                            dt = dt.plusMonths(Integer.parseInt(value));
                            break;
                        case "days":
                            dt = dt.plusDays(Integer.parseInt(value));
                            break;
                        case "hours":
                            dt = dt.plusHours(Integer.parseInt(value));
                            break;
                        case "minutes":
                            dt = dt.plusMinutes(Integer.parseInt(value));
                            break;
                    }
                }
                break;
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
            System.out.println("what");
            throw new exceptions.BadParameterException();
        }
        return res;
    }
}
