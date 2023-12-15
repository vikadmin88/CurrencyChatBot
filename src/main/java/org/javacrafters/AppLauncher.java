package org.javacrafters;

import org.javacrafters.scheduler.Scheduler;
import org.javacrafters.scheduler.Scheduler2;
import org.javacrafters.user.User;
import org.javacrafters.utils.ConfigLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;


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