package org.javacrafters.core;

import org.javacrafters.AppLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigLoader.class);
    private static Properties conf = null;

    public static Properties getConf() {
        if (conf == null) {
            conf = loadConfig();
        }
        return conf;
    }

    public static String get(String key) {
        if (conf != null && !conf.isEmpty()) {
            return (String) conf.get(key);
        }
        conf = loadConfig();
        return (String) conf.get(key);
    }

    private static Properties loadConfig() {
        Properties prop = new Properties();
        try (FileInputStream input = new FileInputStream("./app.properties")) {
            prop.load(input);
        } catch (IOException ex) {
            LOGGER.error("Can't find config file", ex);
            System.out.println(""" 
                    Config file ./app.properties not found! Please rename file ./app.properties-example to ./app.properties
                    and configure it like this:
                    # Bot credentials:
                    APP_NAME=CurrencyChatBot
                    APP_BOT_NAME=here your bot name
                    APP_BOT_TOKEN=here your bot token
                                       
                    # User storage functionality:
                    # Enable/disable. save/load to/from json, database, etc.
                    APP_USERS_USE_STORAGE=true
                    # storage folder
                    APP_USERS_STORAGE_FOLDER=./bot-users
                    # storage provider. file | sqlite
                    APP_USERS_STORAGE_PROVIDER=sqlite
                                       
                    # Default parameters for banks:
                    # Currency to get from each bank. separate by comma: USD,EUR,...
                    BANK_CURRENCY=USD,EUR,GBP,PLN
                    # Frequency of requests to banks for latest exchange rates (minutes)
                    BANK_FREQUENCY_REQUEST=1
                    # URLs of API bank pages
                    BANK_PB_API_URL=https://api.privatbank.ua/p24api/pubinfo?json&exchange&coursid=5
                    BANK_MB_API_URL=https://api.monobank.ua/bank/currency
                    BANK_NBU_API_URL=https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json
                                       
                    # Default parameters for each new user
                    # only one (example: PB - Privat Bank, MB - Mono Bank, NBU - National Bank of Ukraine)
                    USER_DEF_BANK=PB
                    # only one of (USD | EUR | GBP | PLN)
                    USER_DEF_CURRENCY=USD
                    # Currency decimal places. One of: 2 | 3 | 4
                    USER_DEF_DECIMAL_PLACES=2
                                       
                    # Time to get exchange rates
                    # one of: 9 | 10 | 11 |...18
                    USER_DEF_NOTIFY_TIME=9
                    # Enable/disable One of: true | false
                    USER_DEF_NOTIFY_ENABLED=true
                    """);
            System.exit(1);
        }
        return prop;
    }
}