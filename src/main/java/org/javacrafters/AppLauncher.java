package org.javacrafters;

import org.javacrafters.banking.MonoBank;
import org.javacrafters.banking.NbuBank;
import org.javacrafters.banking.PrivatBank;
import org.javacrafters.core.AppRegistry;
import org.javacrafters.core.ChatBot;
import org.javacrafters.core.UserLoader;
import org.javacrafters.core.storage.JsonStorageProvider;
import org.javacrafters.networkclient.NetworkStreamReader;
import org.javacrafters.core.ConfigLoader;


public class AppLauncher {


    public static void main(String[] args) {
        System.out.println("Program starting in Thread: " + Thread.currentThread().getName());

        AppRegistry.initDefaults();
        AppRegistry.setNetClient(new NetworkStreamReader());
        AppRegistry.addBank("PB", new PrivatBank());
        AppRegistry.addBank("MB", new MonoBank());
        AppRegistry.addBank("NBU", new NbuBank());

        UserLoader.setStorageProvider(new JsonStorageProvider()).load();

        String appName = ConfigLoader.get("APP_NAME");
        String botName = ConfigLoader.get("APP_BOT_NAME");
        String botToken = ConfigLoader.get("APP_BOT_TOKEN");

        ChatBot bot = new ChatBot(appName, botName, botToken);
        bot.botRun();
    }

}