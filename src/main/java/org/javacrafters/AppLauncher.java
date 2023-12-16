package org.javacrafters;

import org.javacrafters.core.ChatBot;
import org.javacrafters.user.User;
import org.javacrafters.core.ConfigLoader;
import java.util.HashMap;
import java.util.Map;


public class AppLauncher {


    public static void main(String[] args) {
        System.out.println("Starting " + Thread.currentThread().getName());

        Map<Long, User> users = new HashMap<>();
        String appName = ConfigLoader.get("appName");
        String botName = ConfigLoader.get("botName");
        String botToken = ConfigLoader.get("botToken");

        ChatBot bot = new ChatBot(users, appName, botName, botToken);
        bot.botRun();
    }

}