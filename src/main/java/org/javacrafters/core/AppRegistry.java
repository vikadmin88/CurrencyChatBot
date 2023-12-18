package org.javacrafters.core;

import java.util.*;

import org.javacrafters.banking.Bank;
import org.javacrafters.networkclient.NetworkClient;
import org.javacrafters.user.User;


public class AppRegistry {
    private static final Map<Long, User> users = new HashMap<>();
    private static final Map <String, Bank> banks = new HashMap<>();
    private static final List<String> currency = new ArrayList<>();

    // USER_DEF_* conf settings
    private static String confBankLocalName = "PB";
    private static String confCurrency = "USD";
    private static int confCountLastDigits = 2;
    private static int confNotifyTime = 9;
    private static boolean confIsNotifyOn = true;

    private static NetworkClient netClient ;

    public static String getConfVal(String key) {
        return ConfigLoader.get(key);
    }

    public static void initDefault() {
        Arrays.stream(ConfigLoader.get("BANK_CURRENCY").split(",")).forEach(AppRegistry::addCurrency);
        AppRegistry.setConfBank(ConfigLoader.get("USER_DEF_BANK"));
        AppRegistry.setConfCurrency(ConfigLoader.get("USER_DEF_CURRENCY"));
        AppRegistry.setConfCountLastDigits(Integer.parseInt(ConfigLoader.get("USER_DEF_COUNT_LAST_DIGITS")));
        AppRegistry.setConfNotifyTime(Integer.parseInt(ConfigLoader.get("USER_DEF_NOTIFY_TIME")));
        AppRegistry.setConfNotifyStatus(Boolean.parseBoolean(ConfigLoader.get("USER_DEF_NOTIFY_ENABLED")));
    }

    public static void addUser(User user){
        users.put(user.getId(), user);
    }

    public static Map<Long, User>getUsers(){
        return users;
    }

    public static User getUser(Long userId){
        return users.get(userId);
    }

    public static boolean hasUser(Long userId){
        return users.get(userId) != null;
    }

    public static void addBank(String bankLocalName, Bank bank){ banks.put(bankLocalName, bank);}

    public static Map <String, Bank> getBanks(){
        return banks;
    }

    public static Bank getBank(String bankLocalName){
        return banks.get(bankLocalName);
    }

    public static void addCurrency(String currencyName){
        currency.add(currencyName);
    }

    public static List<String> getCurrency(){
        return currency;
    }

    public static String getCurrency(int currencyId){
        return currency.get(currencyId);
    }


    public static String getConfBank() {
        return confBankLocalName;
    }

    public static void setConfBank(String bankLocalName) {
        confBankLocalName = bankLocalName;
    }

    public static void setConfCurrency(String currency) {
        confCurrency = currency;
    }

    public static String getConfCurrency() {
        return confCurrency;
    }
    public static void setConfCountLastDigits(int num) {
        confCountLastDigits = num;
    }

    public static int getConfCountLastDigits() {
        return confCountLastDigits;
    }

    public static void setConfNotifyTime(int num) {
        confNotifyTime = num;
    }

    public static int getConfNotifyTime() {
        return confNotifyTime;
    }

    public static boolean getConfNotifyStatus() {
        return confIsNotifyOn;
    }

    public static void setConfNotifyStatus(boolean confNotifyStatus) {
        confIsNotifyOn = confNotifyStatus;
    }

    public static void setNetClient(NetworkClient netClt) {
        netClient = netClt;
    }

    public static NetworkClient getNetClient(){
        return netClient;
    }

}