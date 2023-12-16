package org.javacrafters;

import org.javacrafters.core.ChatBot;
import org.javacrafters.scheduler.Scheduler;
import org.javacrafters.user.User;
import org.javacrafters.core.ConfigLoader;
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

//        User user = new User(1L, "Viktest1", "UserName1");
//        ScheduledFuture<?> notifyTask = new Scheduler().schedule(bot, user, 9);

//        user.setScheduledTask(notifyTask);
//        System.out.println("user = " + user);
//        System.out.println("user.getScheduledTask() = " + user.getScheduledTask());
//        System.out.println("user.getScheduledTask().cancel(true) = " + user.getScheduledTask().cancel(true));
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        System.out.println("notifyTask.isDone() = " + notifyTask.isDone());

        bot.botRun();
    }

}