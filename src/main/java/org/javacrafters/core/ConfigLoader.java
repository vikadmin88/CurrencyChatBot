package org.javacrafters.core;

import org.javacrafters.AppLauncher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {

    private static Properties conf = null;

    private ConfigLoader() {
    }

    public static Properties getConf(){
        if (conf == null) {
            conf = loadConfig();
        }
        return conf;
    }
    public static String get(String key){
        if (conf != null && !conf.isEmpty()) {
            return (String) conf.get(key);
        }
        conf = loadConfig();
        return (String) conf.get(key);
    }

    private static Properties loadConfig() {
        Properties prop = new Properties();
        try (FileInputStream input = new FileInputStream("src/app.properties")) {
            prop.load(input);
        } catch (IOException ex) {
            System.out.println(""" 
                    APP_NAME=CurrencyChatBot
                    APP_BOT_NAME=<bot name>
                    APP_BOT_TOKEN=<bot token>
                                            
                    # separate by coma
                    BANK_CURRENCY=USD,EUR,GBP,PLN
                    BANK_PB_API_URL=https://api.privatbank.ua/p24api/pubinfo?json&exchange&coursid=5
                    BANK_MB_API_URL=https://api.monobank.ua/bank/currency
                    BANK_NBU_API_URL=https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json
                                            
                    # only one (bank_local_name: PB - PrivatBank)
                    USER_DEF_BANK=PB
                    # only one (USD)
                    USER_DEF_CURRENCY=USD
                    # one of: 2 | 3 | 4
                    USER_DEF_COUNT_LAST_DIGITS=2
                    # one of: 9 | 10 | 11 |...18
                    USER_DEF_NOTIFY_TIME=9
                    # true | false
                    USER_DEF_NOTIFY_ENABLED=true
                        """);
            System.exit(1);
        }
        return prop;
    }
}
