package discordFrontend;

import org.javacord.api.entity.channel.TextChannel;

import java.io.Serializable;
import java.util.List;

public class AutoReact implements Serializable {
    private List<Long> ids;
    private String content;
    public AutoReact(List<Long> lids, String input){
        this.ids = lids;
        this.content = input;
    }

    public List<Long> getIds() {
        return ids;
    }

    public boolean lidsNull(){
        return ids.isEmpty();
    }

    public void addId(Long id){
        ids.add(id);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void trigger(TextChannel channel, Long uid){
        if (ids.isEmpty() || ids.contains(uid))
            channel.sendMessage(content);
    }

}
