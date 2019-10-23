package com.lwansbrough.serverfarm.client.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.scheduling.annotation.Async;

class ShellHelper {
    @Async
    public static String execute(String cmd) throws IOException {
        String escapedArgs = cmd.replace("\"", "\\\"");

        Process process = new ProcessBuilder()
            .command("/bin/bash", "-c", escapedArgs)
            .start();

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

        StringBuilder sb = new StringBuilder();
        String s;
        while ((s = stdInput.readLine()) != null) {
            sb.append(s);
        }

        process.destroy();

        return sb.toString();
    }
}