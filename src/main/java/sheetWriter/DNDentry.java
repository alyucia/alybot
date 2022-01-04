package sheetWriter;

import java.time.Instant;

public class DNDentry {
    private Instant createTime;
    private String name = "N/A";
    private String skill = "N/A";
    private String dice = "N/A";
    private String roll = "N/A";
    private String mod = "N/A";
    private String total = "N/A";
    private String msgId;
    private boolean advantage = false;


    public DNDentry(Instant cTime, String n, String s, String d, String r, String m, String t, String mi){
        this.createTime = cTime;
        this.name = n;
        this.skill = s;
        this.dice = d;
        this.roll = r;
        this.mod = m;
        this.total = t;
        this.msgId = mi;
    }

    @Override
    public String toString() {
        return "DNDentry{" +
                "createTime=" + createTime +
                ", name='" + name + '\'' +
                ", skill='" + skill + '\'' +
                ", dice='" + dice + '\'' +
                ", roll='" + roll + '\'' +
                ", mod='" + mod + '\'' +
                ", total='" + total + '\'' +
                ", msgId='" + msgId + '\'' +
                ", advantage=" + advantage +
                '}';
    }
}
