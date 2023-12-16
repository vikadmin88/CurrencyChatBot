package org.javacrafters.banking;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.javacrafters.networkclient.NetworkClient;
import org.javacrafters.core.ConfigLoader;

import java.util.*;

public class PrivatBank extends Bank {
    private NetworkClient netClient;
    private final static String NAME = "Приват Банк";
    private final static String LOCAL_NAME  = "PB";
//    private final static String API_URL = "https://api.privatbank.ua/p24api/pubinfo?json&exchange&coursid=5";
    private final static String API_URL = ConfigLoader.get("PB_API_URL");

    public PrivatBank() {
    }

    public PrivatBank(NetworkClient netClient) {
        this.netClient = netClient;
    }
    @Override
    public void setNetClient(NetworkClient netClient) {
        this.netClient = netClient;
    }

    @Override
    public Map<String, NormalizeCurrencyPair> getRates() {
        Gson gson = new Gson();
        JsonObject[] jsonObjArr = gson.fromJson(netClient.get(API_URL), JsonObject[].class);

        // {"USD" => ""USD"", "36.95000", "37.45000"}
        Map<String, NormalizeCurrencyPair> rateMap = new HashMap<>();

        for (JsonObject jsonObjItem : jsonObjArr) {
            JsonObject jsonObj = gson.fromJson(jsonObjItem, JsonObject.class);
            String currencyName = jsonObj.get("ccy").getAsString();
            String currencyBuy = jsonObj.get("buy").getAsString();
            String currencySale = jsonObj.get("sale").getAsString();
            rateMap.put(currencyName, new NormalizeCurrencyPair(currencyName, currencyBuy, currencySale));
        }
        return rateMap;
    }

    public String getName() {
        return NAME;
    }

    public String getLocalName() {
        return LOCAL_NAME;
    }

    @Override
    public String toString() {
        return "PrivatBank{" +
                "NAME='" + NAME + '\'' +
                ", LOCAL_NAME='" + LOCAL_NAME + '\'' +
                '}';
    }
}
