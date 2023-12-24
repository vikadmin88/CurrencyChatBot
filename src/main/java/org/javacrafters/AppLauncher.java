package org.javacrafters;

import org.javacrafters.banking.MonoBank;
import org.javacrafters.banking.NbuBank;
import org.javacrafters.banking.PrivatBank;
import org.javacrafters.core.AppRegistry;
import org.javacrafters.core.ChatBot;
import org.javacrafters.networkclient.NetworkStreamReader;
import org.javacrafters.core.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AppLauncher {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppLauncher.class);

    public static void main(String[] args) {
        LOGGER.info("Program starting in Thread: {}", Thread.currentThread().getName());

        AppRegistry.initDefaults();
        AppRegistry.addBank("PB", new PrivatBank(ConfigLoader.get("BANK_PB_API_URL"), new NetworkStreamReader()));
        AppRegistry.addBank("MB", new MonoBank(ConfigLoader.get("BANK_MB_API_URL"), new NetworkStreamReader()));
        AppRegistry.addBank("NBU", new NbuBank(ConfigLoader.get("BANK_NBU_API_URL"), new NetworkStreamReader()));

        String appName = ConfigLoader.get("APP_NAME");
        String botName = ConfigLoader.get("APP_BOT_NAME");
        String botToken = ConfigLoader.get("APP_BOT_TOKEN");

        ChatBot bot = new ChatBot(appName, botName, botToken);
        bot.botRun();
    }

}