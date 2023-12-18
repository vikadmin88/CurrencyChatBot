package org.javacrafters.core;

import java.util.*;

import org.javacrafters.banking.Bank;
import org.javacrafters.networkclient.NetworkClient;
import org.javacrafters.user.User;


public class AppRegistry {
    private static final Map<Long, User> users = new HashMap<>();
    private static final Map <String, Bank> banks = new HashMap<>();

    private static final List<String> currency = new ArrayList<>();

    private static int countDigits = 2;

    private static NetworkClient netClient ;

    public static String getConfVal(String key) {
        return ConfigLoader.get(key);
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

    public static List<String> addCurrency(String currencyName){
        currency.add(currencyName);
        return currency;
    }

    public static List<String> getCurrency(){
        return currency;
    }

    public static String getCurrency(int currencyId){
        return currency.get(currencyId);
    }

    public static void setCountDigits(int num) {
        countDigits = num;
    }

    public static int getCountDigits() {
        return countDigits;
    }

    public static void setNetClient(NetworkClient netClt) {
        netClient = netClt;
    }

    public static NetworkClient getNetClient(){
        return netClient;
    }

}