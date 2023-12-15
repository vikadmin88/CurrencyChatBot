package org.javacrafters.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {

    private static Properties conf = null;
    public static String get(String key){
        if (conf != null && !conf.isEmpty()) {
            return (String) conf.get(key);
        }
        conf = load();
        return (String) conf.get(key);
    }

    private static Properties load() {
        Properties prop = new Properties();
        try (FileInputStream input = new FileInputStream("./app.properties")) {
            prop.load(input);
        } catch (IOException ex) {
            System.out.println(""" 
                        Sorry, unable to find config - app.properties
                        Please create it in root folder ./app.properties
                        If you are running the application as a fat jar file, please
                        copy/create app.properties next to the application jar file.
                        Then add the following to it:
                        appName=CurrencyChatBot
                        botName=<your telegram chat bot name>
                        botToken=<the bot token>
                        PB_API_URL=https://api.privatbank.ua/p24api/pubinfo?json&exchange&coursid=5
                        MB_API_URL=https://api.monobank.ua/bank/currency
                        NBU_API_URL=https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json
                        """);
            System.exit(1);
        }
        return prop;
    }
}
