package discordFrontend.dndscraper;

import com.vdurmont.emoji.EmojiParser;
import discordFrontend.dndscraper.exceptions.NotARollException;
import discordFrontend.dndscraper.exceptions.RollWaitException;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.MessageEditListener;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;
import sheetWriter.DNDentry;
import sheetWriter.SheetWriter;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DNDScraper {
    //Hardcoded to check specifically for Avrae DND bot
    private final String DND_BOT_ID = "261302296103747584";

    //RegEx strings
    private final String INITIATIVE_REGEX = "\\*\\*([A-Za-z]*(?:\\s[A-Za-z]*)?):\\sInitiative:\\sRoll\\*\\*:\\s1d20\\s\\(([*0-9]*)\\)\\s?([+-]\\s[0-9]{0,2})\\n\\*\\*Total\\*\\*:\\s([0-9]*)$";
    private final String TITLE_REGEX = "^([A-Z]{1}[a-z]*(?:\\s[A-Z]{1}[a-z]*)?)\\s(attacks|casts|heals|makes)\\s(?:with\\s)?(?:a\\s)?(?:an\\s)?([a-zA-Z\\s]*)!$";
    private final String ATTACK_REGEX = "^(?:(?:\\*\\*To\\sHit\\*\\*:\\s)([0-9]*d[0-9]*)\\s\\(([0-9\\*]*(?:,\\s[\\*0-9]*)*)\\)(\\s[+-]\\s[0-9]*)?\\s=\\s`([0-9]*)`\\n)?\\*\\*Damage(?:\\s\\(CRIT!\\))?\\*\\*:\\s([0-9]*d[0-9]*)\\s\\(([\\*0-9]*(?:,\\s[\\*0-9]*)*)\\)(\\s[+-]\\s[0-9]*)?\\s=\\s`([0-9]*)`$";
    private final String CHECK_REGEX = "^([0-9]*d[0-9]*)\\s\\(([*0-9]*)\\)\\s([+-]\\s[0-9]\\s)?=\\s`([0-9]*)`";
    private final TextChannel scrapedChannel;
    private final SheetWriter sheetWriter;
    private final String BOT_ID;
    private Map<String, List<DNDentry>> entries;

    public DNDScraper(TextChannel channel, SheetWriter writer, String botId){
        this.scrapedChannel = channel;
        this.sheetWriter = writer;
        this.entries = new LinkedHashMap<>();
        this.BOT_ID = botId;
    }

    public void startListening(){
        var listener = new AtomicReference<MessageCreateListener>();
        listener.set(event1 -> {
            if(event1.getMessageAuthor().getIdAsString().equals(DND_BOT_ID)){
                Thread msgReader = new Thread(()-> {
                    Message botMsg = event1.getMessage();
                    try {
                        parseMessage(botMsg);
                        addReactionListeners(botMsg);
                    } catch (RollWaitException e) {
                        var listener2 = new AtomicReference<MessageEditListener>();
                        listener2.set(event2 -> {
                            try {
                                parseMessage(event1.getMessage());
                                addReactionListeners(botMsg);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            botMsg.removeMessageAttachableListener(listener2.get());
                        });
                        botMsg.addMessageEditListener(listener2.get()).removeAfter(5, TimeUnit.MINUTES);
                    } catch (Exception ex){
                        ex.printStackTrace();
                    }
                });
                msgReader.start();
            }
            if(event1.getMessageContent().equalsIgnoreCase("!endsession")){
                event1.getMessage().addReaction(EmojiParser.parseToUnicode(":white_check_mark:"));
                for (List<DNDentry> eList: entries.values()) {
                    for (DNDentry entry : eList) {
                        try {
                            if (entry.getUpload()) {
                                sheetWriter.writeInfo(entry.asStringArrayList());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                entries.clear();
                this.scrapedChannel.removeTextChannelAttachableListener(listener.get());
            }
        });
        this.scrapedChannel.addMessageCreateListener(listener.get());
    }

    private void parseMessage(Message msg) throws RollWaitException, NotARollException {
        List<DNDentry> eList = new ArrayList<>();
        Pattern pattern = Pattern.compile(INITIATIVE_REGEX);
        Matcher matcher = pattern.matcher(msg.getContent());
        //Check if it's an initiative roll, and parse it if it is.
        Instant createTime = msg.getCreationTimestamp();
        String msgId = msg.getIdAsString();
        if (matcher.find()){
            String name = matcher.group(1);
            String roll = matcher.group(2);
            String mod = matcher.group(3);
            String total = matcher.group(4);
            eList.add(new DNDentry(createTime, name, "Initiative", "1d20", roll, mod, total, msgId));
        }
        else {
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
            if (type.equalsIgnoreCase("makes")) {
                String meta = msg.getEmbeds().get(0).getDescription().get();
                pattern = Pattern.compile(CHECK_REGEX);
                matcher = pattern.matcher(meta);
                if (!matcher.find())
                    throw new NotARollException();
                String dice = matcher.group(1);
                String roll = matcher.group(2);
                String mod = matcher.group(3);
                String total = matcher.group(4);
                eList.add(new DNDentry(createTime, name, skill, dice, roll, mod, total, msgId));
            } else if (type.equalsIgnoreCase("heals")) {
                //TODO: healing shit
            } else {
                String meta = msg.getEmbeds().get(0).getFields().get(0).getValue();
                if (meta.contains("Waiting for roll...")) {
                    throw new RollWaitException();
                }
                pattern = Pattern.compile(ATTACK_REGEX);
                matcher = pattern.matcher(meta);
                if (!matcher.matches())
                    throw new NotARollException();
                if (meta.contains("To Hit")) {
                    String attackDice = matcher.group(1);
                    String attackRoll = matcher.group(2);
                    String attackMod = matcher.group(3);
                    String attackTotal = matcher.group(4);
                    eList.add(new DNDentry(createTime, name, skill + " to hit", attackDice, attackRoll, attackMod, attackTotal, msgId));
                }
                String damageDice = matcher.group(5);
                String damageRoll = matcher.group(6);
                String damageMod = matcher.group(7);
                String damageTotal = matcher.group(8);
                eList.add(new DNDentry(createTime, name, skill + " damage", damageDice, damageRoll, damageMod, damageTotal, msgId));
            }
        }
        entries.put(msgId, eList);

    }
    public void addReactionListeners(Message msg){
        String msgId = msg.getIdAsString();
        msg.addReaction(EmojiParser.parseToUnicode(":x:"));
        msg.addReaction(EmojiParser.parseToUnicode(":regional_indicator_symbol_a:"));
        var addListener = new AtomicReference<ReactionAddListener>();
        addListener.set(event -> {
            if (event.getUserIdAsString().equalsIgnoreCase(BOT_ID))
                return;
            List<DNDentry> eList = entries.get(msgId);
            if(event.getEmoji().equalsEmoji(EmojiParser.parseToUnicode(":x:"))){
                for(DNDentry ent : eList) {
                    ent.setUpload(false);
                }
            } else if(event.getEmoji().equalsEmoji(EmojiParser.parseToUnicode(":regional_indicator_symbol_a:"))){
                for(DNDentry ent : eList){
                    ent.setAdvantage(true);
                }
            }
        });
        var removeListener = new AtomicReference<ReactionRemoveListener>();
        removeListener.set(event -> {
            if (event.getUserIdAsString().equalsIgnoreCase(BOT_ID))
                return;
            List<DNDentry> eList = entries.get(msgId);
            if(event.getEmoji().equalsEmoji(EmojiParser.parseToUnicode(":x:"))){
                for(DNDentry ent : eList) {
                    ent.setUpload(true);
                }
            } else if(event.getEmoji().equalsEmoji(EmojiParser.parseToUnicode(":regional_indicator_symbol_a:"))){
                for(DNDentry ent : eList){
                    ent.setAdvantage(false);
                }
            }
        });
        msg.addReactionAddListener(addListener.get()).removeAfter(15, TimeUnit.MINUTES);
        msg.addReactionRemoveListener(removeListener.get()).removeAfter(15, TimeUnit.MINUTES);
    }
}
