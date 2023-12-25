package org.javacrafters.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import org.javacrafters.banking.Bank;
import org.javacrafters.core.storage.JsonStorageProvider;
import org.javacrafters.core.storage.SQLiteStorageProvider;
import org.javacrafters.networkclient.NetworkClient;
import org.javacrafters.scheduler.Scheduler;
import org.javacrafters.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mykhailo Orban
 */
public class AppRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppRegistry.class);

    private static ChatBot chatBot;
    private static final Map<Long, User> users = new HashMap<>();
    private static final Map <String, Bank> banks = new HashMap<>();
    private static final List<String> currency = new ArrayList<>();
    private static NetworkClient netClient ;

    public static String getConfVal(String key) {
        return ConfigLoader.get(key);
    }


    private AppRegistry() {
    }

    public static void initDefaults() {
        if (AppRegistry.getConfIsDevMode()) {
            LOGGER.info("!!! {} working in DEV mode !!!", ConfigLoader.get("APP_NAME"));
        }

        LOGGER.info("Loading config defaults: initDefaults()");
        Arrays.stream(ConfigLoader.get("BANK_CURRENCY").split(",")).forEach(AppRegistry::addCurrency);

        // Request currency period in minutes
        Scheduler.addCurrencySchedule(Integer.parseInt(ConfigLoader.get("BANK_FREQUENCY_REQUEST")));
        LOGGER.info("The Currency Query Scheduler runs every {} minutes.", ConfigLoader.get("BANK_FREQUENCY_REQUEST"));

        // user-storage folder, provider
        if (Boolean.parseBoolean(ConfigLoader.get("APP_USERS_USE_STORAGE"))) {
            try {
                String storageFolder = AppRegistry.getConfUsersStorageFolder();
                Files.createDirectories(Paths.get(storageFolder));
                if (ConfigLoader.get("APP_USERS_STORAGE_PROVIDER").equals("file")) {
                    UserLoader.setStorageProvider(new JsonStorageProvider(storageFolder)).load();
                    LOGGER.info("User-storage provider ({}) added: JsonStorageProvider()", ConfigLoader.get("APP_USERS_STORAGE_PROVIDER"));
                } else if (ConfigLoader.get("APP_USERS_STORAGE_PROVIDER").equals("sqlite")) {
                    UserLoader.setStorageProvider(new SQLiteStorageProvider(storageFolder)).load();
                    LOGGER.info("User-storage provider ({}) added: SQLiteStorageProvider()", ConfigLoader.get("APP_USERS_STORAGE_PROVIDER"));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /*
    ChatBot
    */
    public static void setChatBot(ChatBot bot){
        chatBot = bot;
    }
    public static ChatBot getChatBot(){
        return chatBot;
    }


    /*
    Users
    */
    public static void addUser(User user){
        users.put(user.getId(), user);
    }
    public static Map<Long, User>getUsers(){
        return new HashMap<Long, User>(users);
    }
    public static User getUser(Long userId){
        return users.get(userId);
    }
    public static boolean hasUser(Long userId){
        return users.get(userId) != null;
    }
    public static void removeUser(Long userId){
        users.remove(userId);
    }

    /*
    Bank
    */
    public static void addBank(String bankLocalName, Bank bank){ banks.put(bankLocalName, bank);}
    public static Map <String, Bank> getBanks(){
        return new HashMap<String, Bank>(banks);
    }
    public static Bank getBank(String bankLocalName){
        return banks.get(bankLocalName);
    }

    /*
    Currency
    */
    public static void addCurrency(String currencyName){
        currency.add(currencyName);
    }
    public static List<String> getCurrency(){
        return new ArrayList<>(currency);
    }
    public static String getCurrency(int currencyId){
        return currency.get(currencyId);
    }

    /*
    Net Client
    */
    public static void setNetClient(NetworkClient netClt) {
        netClient = netClt;
    }
    public static NetworkClient getNetClient() {
        return netClient;
    }

    /*
    Conf bank
    */
    public static String getConfBank() {
        return ConfigLoader.get("USER_DEF_BANK");
    }

    /*
    Config currency
    */
    public static String getConfCurrency() {
        return ConfigLoader.get("USER_DEF_CURRENCY");
    }

    /*
    Config count last digits
    */
    public static int getConfDecimalPlaces() {
        return Integer.parseInt(ConfigLoader.get("USER_DEF_DECIMAL_PLACES"));
    }

    /*
     Config Notify Time
    */
    public static int getConfNotifyTime() {
        return Integer.parseInt(ConfigLoader.get("USER_DEF_NOTIFY_TIME"));
    }

    /*
     Config Notify Status
    */
    public static boolean getConfNotifyStatus() {
        return Boolean.parseBoolean(ConfigLoader.get("USER_DEF_NOTIFY_ENABLED"));
    }

    /*
     Config Save/load users to/from json
    */
    public static boolean getConfIsUsingUsersStorage() {
        return Boolean.parseBoolean(ConfigLoader.get("APP_USERS_USE_STORAGE"));
    }
    public static String getConfUsersStorageFolder() {
        return ConfigLoader.get("APP_USERS_STORAGE_FOLDER");
    }

    /*
    Config Dev mode switch
    */
    public static boolean getConfIsDevMode() {
        return Boolean.parseBoolean(ConfigLoader.get("APP_DEV_MODE"));
    }
    public static boolean getConfIsProdMode() {
        return !Boolean.parseBoolean(ConfigLoader.get("APP_DEV_MODE"));
    }


}