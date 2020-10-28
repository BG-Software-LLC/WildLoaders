package com.bgsoftware.wildloaders.utils;

import com.bgsoftware.wildloaders.Locale;
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

    public static String formatTime(long time){
        StringBuilder stringBuilder = new StringBuilder();

        if(time >= DAYS){
            long days = time / DAYS;
            stringBuilder.append(", ").append(days).append(" ")
                    .append((days == 1 ? Locale.TIME_PLACEHOLDER_DAY : Locale.TIME_PLACEHOLDER_DAYS).getMessage());
            time = time % DAYS;
        }

        if(time >= HOURS){
            long hours = time / HOURS;
            stringBuilder.append(", ").append(hours).append(" ")
                    .append((hours == 1 ? Locale.TIME_PLACEHOLDER_HOUR : Locale.TIME_PLACEHOLDER_HOURS).getMessage());
            time = time % HOURS;
        }

        if(time >= MINUTES){
            long minutes = time / MINUTES;
            stringBuilder.append(", ").append(minutes).append(" ")
                    .append((minutes == 1 ? Locale.TIME_PLACEHOLDER_MINUTE : Locale.TIME_PLACEHOLDER_MINUTES).getMessage());
            time = time % MINUTES;
        }

        if(time > 0){
            stringBuilder.append(", ").append(time).append(" ")
                    .append((time == 1 ? Locale.TIME_PLACEHOLDER_SECOND : Locale.TIME_PLACEHOLDER_SECONDS).getMessage());
        }

        return stringBuilder.substring(2);
    }

}
