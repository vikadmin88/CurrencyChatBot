package org.javacrafters.banking;

import org.javacrafters.core.AppRegistry;

import java.util.Map;
import java.util.HashMap;

public class CurrencyHolder {

    // {"PB" => {"USD" => {"USD", "36.95000", "37.45000"}}}
    private final static Map<String, Map<String, NormalizeCurrencyPair>> currency = new HashMap<>();

    public static void refreshRates() {

        for (Map.Entry<String, Bank> bankObj : AppRegistry.getBanks().entrySet()) {
            Bank bank = bankObj.getValue();
            Map<String, NormalizeCurrencyPair> rates = bank.getRates();
            add(bank.getLocalName(), rates);
        }
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