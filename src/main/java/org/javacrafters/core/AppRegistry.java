package org.javacrafters.core;

import java.util.*;

import org.javacrafters.banking.Bank;
import org.javacrafters.networkclient.NetworkClient;
import org.javacrafters.user.User;


public class AppRegistry {
    private static final Map<Long, User> users = new HashMap<>();
    private static final Map <String, Bank> banks = new HashMap<>();

    private static final List<String> currency = new ArrayList<>();

    private static final List<Integer> countDigits = new ArrayList<>();

    private static NetworkClient netClient ;

    public static void init() {

    }
    public static void addUser(Long userId, User user){
        users.put(userId, user);
    }

    public static Map<Long, User>getUsers(){
        return users;
    }

    public static User getUser(Long userId){
        return users.get(userId);
    }

    public static void addBank(String bankLocalName, Bank bank){banks.put(bankLocalName ,bank);}

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

    public static List<Integer> addCountDigits(Integer num ){
        countDigits.add(num);
        return countDigits;

    }

    public static List <Integer> getCountDigits(){
        return countDigits;
    }

    public static Integer getCountDigits(int id){
        return countDigits.get(id);
    }

    public static void addNetClient(NetworkClient netClient){

        AppRegistry.netClient = netClient;
    }

    public static NetworkClient getNetClient(){
        return netClient;
    }

}