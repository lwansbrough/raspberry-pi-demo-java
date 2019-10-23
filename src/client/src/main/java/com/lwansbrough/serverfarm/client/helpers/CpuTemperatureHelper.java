package com.lwansbrough.serverfarm.client.helpers;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CpuTemperatureHelper {
    private static final Pattern TEMPERATURE_REGEX = Pattern.compile("temp=(\\d+\\.\\d+)'C", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    public static float measure() {
        String rawTemp = "temp=0.0'C";
        try {
            rawTemp = ShellHelper.execute("/opt/vc/bin/vcgencmd measure_temp");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Matcher rawTempMatcher = TEMPERATURE_REGEX.matcher(rawTemp);
        if (rawTempMatcher.find()) {
            String matched = rawTempMatcher.group(1);
            float temp = Float.parseFloat(matched);
            return temp;
        }
        else {
            return 0f;
        }
    }
}
