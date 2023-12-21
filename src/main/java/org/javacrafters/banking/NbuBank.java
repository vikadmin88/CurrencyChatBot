package org.javacrafters.banking;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.javacrafters.networkclient.NetworkClient;

import java.util.HashMap;
import java.util.Map;

public class NbuBank extends Bank {

    private final static String NAME = "НБУ Національний Банк України";
    private final static String LOCAL_NAME = "NBU";
    private final String apiUrl;
    private final NetworkClient netClient;

    public NbuBank(String apiUrl, NetworkClient netClient) {
        this.apiUrl = apiUrl;
        this.netClient = netClient;
    }

    @Override
    public Map<String, NormalizeCurrencyPair> getRates() {

        Gson gson = new Gson();
        JsonObject[] jsonObjArr = gson.fromJson(netClient.get(apiUrl), JsonObject[].class);

        // {"USD" => ""USD"", "36.95000", "37.45000"}
        Map<String, NormalizeCurrencyPair> rateMap = new HashMap<>();

        for (JsonObject jsonObjItem : jsonObjArr) {
            JsonObject jsonObj = gson.fromJson(jsonObjItem, JsonObject.class);
            String currencyName = jsonObj.get("cc").getAsString();
            String currencyBuy = "-1";
            String currencySale = jsonObj.get("rate").getAsString();
            rateMap.put(currencyName, new NormalizeCurrencyPair(currencyName, currencyBuy, currencySale));
        }
        return rateMap;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getLocalName() {
        return LOCAL_NAME;
    }

    @Override
    public String toString() {
        return "NbuBank{" +
                "NAME='" + NAME + '\'' +
                ", LOCAL_NAME='" + LOCAL_NAME + '\'' +
                '}';
    }
}
