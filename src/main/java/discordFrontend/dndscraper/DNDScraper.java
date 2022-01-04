package discordFrontend.dndscraper;

import discordFrontend.dndscraper.exceptions.NotARollException;
import discordFrontend.dndscraper.exceptions.RollWaitException;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.MessageEditListener;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DNDScraper {
    //Hardcoded to check specifically for Avrae DND bot
    private final String BOT_ID = "261302296103747584";

    //RegEx strings
    private final String INITIATIVE_REGEX = "\\*\\*([A-Za-z]*(?:\\s[A-Za-z]*)?):\\sInitiative: Roll\\*\\*:\\s1d20\\s\\(([0-9]{1,2})\\)\\s?([\\+\\-]\\s[0-9]{0,2})\\n\\*\\*Total\\*\\*:\\s([0-9]*)$";
    private final String TITLE_REGEX = "^([A-Z]{1}[a-z]*(?:\\s[A-Z]{1}[a-z]*)?)\\s(attacks|casts|heals|makes)\\s(?:with\\s)?(?:a\\s)?(?:an\\s)?([a-zA-Z\\s]*)!$";
    private final TextChannel scrapedChannel;

    public DNDScraper(TextChannel channel){
        this.scrapedChannel = channel;
    }

    public void startListening(){
        var listener = new AtomicReference<MessageCreateListener>();
        listener.set(event1 -> {
            if(event1.getMessageAuthor().getIdAsString().equals(BOT_ID)){
                Thread msgReader = new Thread(()-> {
                    Message botMsg = event1.getMessage();
                    try {
                        parseMessage(botMsg);
                    } catch (RollWaitException e) {
                        var listener2 = new AtomicReference<MessageEditListener>();
                        listener2.set(event2 -> {
                            try {
                                parseMessage(event1.getMessage());
                            } catch (RollWaitException | NotARollException ex) {
                                ex.printStackTrace();
                            }
                            botMsg.removeMessageAttachableListener(listener2.get());
                        });
                        botMsg.addMessageEditListener(listener2.get()).removeAfter(15, TimeUnit.MINUTES);
                    } catch (NotARollException ex){
                        ex.printStackTrace();
                    }
                    //Should writing to sheet be inside or outside of message?
                });
                msgReader.start();
            }
            if(event1.getMessageContent().equalsIgnoreCase("!endsession")){
                event1.getMessage().addReaction("\u2705");
                this.scrapedChannel.removeTextChannelAttachableListener(listener.get());
            }
        });
        this.scrapedChannel.addMessageCreateListener(listener.get());
    }

    private void parseMessage(Message msg) throws RollWaitException, NotARollException {
        Pattern pattern = Pattern.compile(INITIATIVE_REGEX);
        Matcher matcher = pattern.matcher(msg.getContent());
        //Check if it's an initiative roll, and parse it if it is.
        if (matcher.find()){
            String name = matcher.group(1);
            String roll = matcher.group(2);
            String mod = matcher.group(3);
            String total = matcher.group(4);
            //TODO: send initiative rolls to sheet
            return;
        }
        String title;
        try {
            title = msg.getEmbeds().get(0).getTitle().get();
        } catch (IndexOutOfBoundsException e) {
            throw new NotARollException();
        }
        pattern = Pattern.compile(TITLE_REGEX);
        matcher = pattern.matcher(title);
        if (!matcher.matches())
            throw new NotARollException();
        String name = matcher.group(1);
        String type = matcher.group(2);
        String skill = matcher.group(3);
        //Check if it's an ability check/save. The embed is different.
        if (type.equalsIgnoreCase("makes")){
            String meta = msg.getEmbeds().get(0).getDescription().get();
            System.out.print(meta);
        } else{
            String meta = msg.getEmbeds().get(0).getFields().get(0).getValue();
            if (meta.contains("Waiting for roll...")){
                throw new RollWaitException();
            }
            System.out.println(meta);
        }
        //TODO: send everything else to sheet
    }
}
