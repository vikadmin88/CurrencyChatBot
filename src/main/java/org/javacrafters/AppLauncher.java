package org.javacrafters;

import org.javacrafters.banking.MonoBank;
import org.javacrafters.banking.NbuBank;
import org.javacrafters.banking.PrivatBank;
import org.javacrafters.core.AppRegistry;
import org.javacrafters.core.ChatBot;
import org.javacrafters.core.UserLoader;
import org.javacrafters.core.storage.JsonStorageProvider;
import org.javacrafters.networkclient.NetworkClient;
import org.javacrafters.networkclient.NetworkStreamReader;
import org.javacrafters.core.ConfigLoader;
import org.javacrafters.scheduler.Scheduler;


public class AppLauncher {


    public static void main(String[] args) {
        System.out.println("Program starting in Thread: " + Thread.currentThread().getName());

        AppRegistry.initDefaults();
        AppRegistry.addBank("PB", new PrivatBank(ConfigLoader.get("BANK_PB_API_URL"), new NetworkStreamReader()));
        AppRegistry.addBank("MB", new MonoBank(ConfigLoader.get("BANK_MB_API_URL"), new NetworkStreamReader()));
        AppRegistry.addBank("NBU", new NbuBank(ConfigLoader.get("BANK_NBU_API_URL"), new NetworkStreamReader()));

        UserLoader.setStorageProvider(new JsonStorageProvider()).load();

        String appName = ConfigLoader.get("APP_NAME");
        String botName = ConfigLoader.get("APP_BOT_NAME");
        String botToken = ConfigLoader.get("APP_BOT_TOKEN");

        ChatBot bot = new ChatBot(appName, botName, botToken);
        bot.botRun();
    }

}