package discordFrontend;


import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONObject;

import java.io.FileWriter;
import java.util.*;
import java.util.regex.*;

public class helpers {
    public static DateTime parseInput(String input){
        DateTime dt = new DateTime();
        if (input.length()== 3){
            return dt.withZone(DateTimeZone.forTimeZone(TimeZone.getTimeZone(input.toUpperCase())));
        }

        HashMap<String, List<Pattern>> rg = new HashMap<>();
        List<Pattern> lsTime = new ArrayList<>();
        Pattern pattern1 = Pattern.compile("(\\d{1,2}):(\\d{2})\\s?([AP]M)?\\s?(\\w{2,3})?", Pattern.CASE_INSENSITIVE);
        lsTime.add(pattern1);
        rg.put("time", lsTime);

        List<Pattern> lsDate = new ArrayList<>();
        Pattern pattern2 = Pattern.compile("(\\d{1,2})?\\s?(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\w*\\s?(\\d{1,2})?", Pattern.CASE_INSENSITIVE);
        lsDate.add(pattern2);
        rg.put("date", lsDate);

        List<Pattern> lsYear = new ArrayList<>();
        Pattern pattern3 = Pattern.compile("(20\\d{2})", Pattern.CASE_INSENSITIVE);
        lsYear.add(pattern3);
        rg.put("year", lsYear);
        /*
        JSONObject obj = new JSONObject(rg);
        try {
            FileWriter file = new FileWriter("src/main/resources/output.json");
            file.write(obj.toString());
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }*/


        for(Map.Entry<String,List<Pattern>> entry : rg.entrySet()){
            for(Pattern p : entry.getValue()){
                dt = switch (entry.getKey()) {
                    case "time" -> regexTime(input, dt, p);
                    case "date" -> regexDate(input, dt, p);
                    case "year" -> regexYear(input, dt, p);
                    default -> dt;
                };
            }
        }

        return dt;
    }

    public static DateTime regexTime(String input, DateTime dt, Pattern p){
        Matcher matcher = p.matcher(input);
        if (matcher.find()) {
            int hour = Integer.parseInt(matcher.group(1));
            int min = Integer.parseInt(matcher.group(2));
            DateTimeZone dtz = dt.getZone();
            if (matcher.group(3) != null){
                hour = hour % 12;
                if (matcher.group(3).equals("PM"))
                    hour = hour + 12;
            }
            if (matcher.group(4) != null){
                dtz = DateTimeZone.forTimeZone(TimeZone.getTimeZone(matcher.group(4)));
            }
            dt = dt.hourOfDay().setCopy(hour);
            dt = dt.minuteOfHour().setCopy(min);
            dt = dt.withZone(dtz);
        }
        return dt;
    }
    public static DateTime regexDate(String input, DateTime dt, Pattern p){
        Matcher matcher = p.matcher(input);
        if (matcher.find()) {
            String month = matcher.group(2);
            System.out.println(month);
            int day = dt.getDayOfMonth();
            if (matcher.group(1) != null)
                day = Integer.parseInt(matcher.group(1));
            if (matcher.group(3) != null)
                day = Integer.parseInt(matcher.group(3));
            dt = dt.monthOfYear().setCopy(month);
            dt = dt.dayOfMonth().setCopy(day);

        }
        return dt;
    }
    public static DateTime regexYear(String input, DateTime dt, Pattern p){
        Matcher matcher = p.matcher(input);
        if (matcher.find()) {
            dt = dt.year().setCopy(Integer.parseInt(matcher.group(1)));

        }
        return dt;
    }

/*
    public static void main(String[] args) {
        try {
            DateTime dt = parseInput(" 12:05 PM PST 2057");
        } catch(Exception e){
            e.printStackTrace();
        }
    }*/
}
