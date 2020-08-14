package com.bgsoftware.wildloaders.utils;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class TimeUtils {

    private static final int MINUTES = 60, HOURS = 3600, DAYS = 86400;

    public static long fromString(String time){
        Map<TimeUnit, Integer> timeUnits = parseString(time);
        long timeValue = 0;

        for(Map.Entry<TimeUnit, Integer> entry : timeUnits.entrySet()){
            switch (entry.getKey()){
                case SECONDS:
                    timeValue += entry.getValue();
                    break;
                case MINUTES:
                    timeValue += entry.getValue() * MINUTES;
                    break;
                case HOURS:
                    timeValue += entry.getValue() * HOURS;
                    break;
                case DAYS:
                    timeValue += entry.getValue() * DAYS;
                    break;
            }
        }

        return timeValue;
    }

    private static Map<TimeUnit, Integer> parseString(String str){
        Map<TimeUnit, Integer> timeUnits = Maps.newHashMap();

        TimeUnit timeUnit;
        StringBuilder value = new StringBuilder();

        for(int i = 0; i < str.length(); i++){
            char ch = str.charAt(i);
            if(Character.isDigit(ch)){
                value.append(ch);
            }

            if(!Character.isDigit(ch) || i == str.length() - 1){
                int timeValue = -1;

                try {
                    timeValue = Integer.parseInt(value.toString());
                }catch(Exception ignored){}

                switch (ch){
                    case 'm':
                    case 'M':
                        timeUnit = TimeUnit.MINUTES;
                        break;
                    case 'h':
                    case 'H':
                        timeUnit = TimeUnit.HOURS;
                        break;
                    case 'd':
                    case 'D':
                        timeUnit = TimeUnit.DAYS;
                        break;
                    default:
                        timeUnit = TimeUnit.SECONDS;
                        break;
                }

                if(timeValue != -1 && !timeUnits.containsKey(timeUnit)){
                    timeUnits.put(timeUnit, timeValue);
                }

                value = new StringBuilder();
            }
        }

        return timeUnits;
    }

    public static String[] formatTime(long time){
        String[] result = new String[4];

        result[0] = String.valueOf(time / DAYS);
        time = time % DAYS;

        result[1] = String.valueOf(time / HOURS);
        time = time % HOURS;

        result[2] = String.valueOf(time / MINUTES);
        time = time % MINUTES;

        result[3] = String.valueOf(time);

        return result;
    }

}
