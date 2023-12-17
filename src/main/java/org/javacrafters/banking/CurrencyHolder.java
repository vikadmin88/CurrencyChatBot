package org.javacrafters.banking;

import java.util.Map;
import java.util.HashMap;

public class CurrencyHolder {


    private static Map<String, Map<String, NormalizeCurrencyPair>> currency = new HashMap<>();


    public static void refreshRates() {
        /*TODO
       Заглушка для методу оновлення курсів валют логіка оновлення курсів валют тут refreshRates() пока оставить
       как заглушку в цикле запрашивает каждый банк на получение валют, делает запрос вида: на объекте банка bankObj.getRates()
       от банка получает коллекцию Map<String, NormalizeCurrencyPair> и добавляет add() ее в свою коллекцию с ключем имени банка
       который получит из bankObj.getLocalName()
         */
        /* можливо так?
        for (Bank bankObj : getAllBanks()) {
            Map<String, NormalizeCurrencyPair> rates = bankObj.getRates();
            currency.put(bankObj.getLocalName());
        }
        TODO bankObj
        */
    }

private static void add(String bankLocalName, Map<String, NormalizeCurrencyPair> rates) {
        currency.put(bankLocalName, rates);
    }

    public static Map<String, Map<String, NormalizeCurrencyPair>> getRates() {
        return currency;
    }

    public static Map<String, NormalizeCurrencyPair> getRates(String bankLocalName) {
        return currency.get(bankLocalName);
    }
}