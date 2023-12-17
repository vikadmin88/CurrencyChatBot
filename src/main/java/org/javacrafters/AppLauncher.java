package org.javacrafters;

import org.javacrafters.core.AppRegistry;
import org.javacrafters.core.ChatBot;
import org.javacrafters.networkclient.NetworkStreamReader;
import org.javacrafters.user.User;
import org.javacrafters.core.ConfigLoader;
import java.util.HashMap;
import java.util.Map;


public class AppLauncher {


    public static void main(String[] args) {
        System.out.println("Program starting in Thread: " + Thread.currentThread().getName());

        Map<Long, User> users = new HashMap<>();


        String appName = ConfigLoader.get("APP_NAME");
        String botName = ConfigLoader.get("APP_BOT_NAME");
        String botToken = ConfigLoader.get("APP_BOT_TOKEN");

        ChatBot bot = new ChatBot(users, appName, botName, botToken);
        bot.botRun();
    }

}