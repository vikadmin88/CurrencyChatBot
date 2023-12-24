package org.javacrafters.banking;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.javacrafters.networkclient.NetworkClient;

import java.util.*;

public class PrivatBank extends Bank {
    private static final String NAME = "Приват Банк";
    private static final String LOCAL_NAME  = "PB";
    private final String apiUrl;
    private final NetworkClient netClient;
    private static final Gson GSON = new Gson();

    public PrivatBank(String apiUrl, NetworkClient netClient) {
        this.apiUrl = apiUrl;
        this.netClient = netClient;
    }

    @Override
    public Map<String, NormalizeCurrencyPair> getRates() {
        JsonObject[] jsonObjArr = GSON.fromJson(netClient.get(apiUrl), JsonObject[].class);

        // {"USD" => "USD", "36.95000", "37.45000"}
        Map<String, NormalizeCurrencyPair> rateMap = new HashMap<>();

        for (JsonObject jsonObjItem : jsonObjArr) {
            JsonObject jsonObj = GSON.fromJson(jsonObjItem, JsonObject.class);
            String currencyName = jsonObj.get("ccy").getAsString();
            String currencyBuy = jsonObj.get("buy").getAsString();
            String currencySale = jsonObj.get("sale").getAsString();
            rateMap.put(currencyName, new NormalizeCurrencyPair(currencyName, currencyBuy, currencySale, null));
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
                ", API_URL='" + apiUrl + '\'' +
                '}';
    }
}
