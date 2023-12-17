package org.javacrafters;

import org.javacrafters.banking.MonoBank;
import org.javacrafters.banking.NbuBank;
import org.javacrafters.banking.PrivatBank;
import org.javacrafters.core.AppRegistry;
import org.javacrafters.core.ChatBot;
import org.javacrafters.networkclient.NetworkStreamReader;
import org.javacrafters.core.ConfigLoader;
import org.javacrafters.scheduler.Scheduler;

import java.util.Arrays;


public class AppLauncher {


    public static void main(String[] args) {
        System.out.println("Program starting in Thread: " + Thread.currentThread().getName());

        AppRegistry.setNetClient(new NetworkStreamReader());
        AppRegistry.addBank("PB", new PrivatBank());
        AppRegistry.addBank("MB", new MonoBank());
        AppRegistry.addBank("NBU", new NbuBank());
        Arrays.stream(ConfigLoader.get("BANK_CURRENCY").split(",")).forEach(AppRegistry::addCurrency);
        AppRegistry.setCountDigits(2);

        // period in minutes
        new Scheduler().currencySchedule( 1);

        String appName = ConfigLoader.get("APP_NAME");
        String botName = ConfigLoader.get("APP_BOT_NAME");
        String botToken = ConfigLoader.get("APP_BOT_TOKEN");

        ChatBot bot = new ChatBot(appName, botName, botToken);
        bot.botRun();
    }

}