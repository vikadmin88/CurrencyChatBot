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

        System.out.println("AppRegistry.getCountDigits() = " + AppRegistry.getCountDigits());
//        System.out.println("AppRegistry.getVal(\"APP_NAME\") = " + AppRegistry.getVal("APP_NAME"));
        System.out.println("ConfigLoader.get(\"APP_NAME\") = " + ConfigLoader.get("APP_NAME"));

        AppRegistry ar = new AppRegistry();
        System.out.println("ar.getVal(\"APP_BOT_TOKEN\") = " + ar.getVal("APP_BOT_TOKEN"));

        String appName = ConfigLoader.get("APP_NAME");
        String botName = ConfigLoader.get("APP_BOT_NAME");
        String botToken = ConfigLoader.get("APP_BOT_TOKEN");

        ChatBot bot = new ChatBot(users, appName, botName, botToken);
        bot.botRun();
    }

}