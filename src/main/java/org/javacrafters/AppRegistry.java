package org.javacrafters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.javacrafters.banking.Bank;
import org.javacrafters.networkclient.NetworkClient;
import org.javacrafters.user.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

    public class AppRegistry {
        private final Map<Long, User> users = new HashMap<>();
        private final Map <String, Bank> banks = new HashMap<>();

        private final List<String> currencies = new ArrayList<>();

        private final List<Integer> countDigits = new ArrayList<>();

        private NetworkClient netClient ;


        void addUser(Long userId, User user){
            users.put(userId, user);
        }

        Map<Long, User>getUsers(){
            return users;
        }
        User getUserById(Long userId){
            return users.get(userId);
        }

        void addBank(String bankLocalName, Bank bank){
            banks.put(bankLocalName ,bank);

        }

        Map <String, Bank> getBanks(){
            return banks;
        }

        Bank getBankByLocalName(String bankLocalName){
            return banks.get(bankLocalName);
        }

        void addCurrencies(String currencyName){

        }

        List<String> getCurrencies(){
            return currencies;
        }

        String getCurrenciesById(int currencyId){
            return banks.get(currencyId).toString();
        }

        void addCountDigits(Integer num ){
            countDigits.add(num);
        }

        List <Integer> getCountDigits(){
            return countDigits;
        }

        Integer getCurrencyById(int id){
            return countDigits.get(id);
        }

        void addNetClient (NetworkClient netClient){
            this.netClient = netClient;
        }

        NetworkClient getNetClient(){
            return netClient;
        }





    }

}
