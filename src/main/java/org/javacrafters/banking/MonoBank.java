package org.javacrafters.banking;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.javacrafters.networkclient.NetworkClient;

import java.util.HashMap;
import java.util.Map;

public class MonoBank extends Bank {
    private final static String NAME = "Моно-Банк";
    private final static String LOCAL_NAME = "MB";
    private final String apiUrl;
    private final NetworkClient netClient;

    public MonoBank(String apiUrl, NetworkClient netClient) {
        this.apiUrl = apiUrl;
        this.netClient = netClient;
    }

    private final static Map<String, Integer> currencies = new HashMap<>();
    static {
        currencies.put("UAH", 980);
        currencies.put("USD", 840);
        currencies.put("EUR", 978);
        currencies.put("GBP", 826);
        currencies.put("PLN", 985);
    }

    @Override
    public Map<String, NormalizeCurrencyPair> getRates() {
        Gson gson = new Gson();
        JsonObject[] jsonObjArr = gson.fromJson(netClient.get(apiUrl), JsonObject[].class);
        // {"USD" => "USD", "36.95000", "37.45000"}
        Map<String, NormalizeCurrencyPair> rateMap = new HashMap<>();

        for (JsonObject jsonObjItem : jsonObjArr) {
            JsonObject jsonObj = gson.fromJson(jsonObjItem, JsonObject.class);
            if (jsonObj.get("rateBuy") != null
                    && jsonObj.get("rateSell") != null
                    && getNameById(jsonObj.get("currencyCodeB").getAsInt()).equals("UAH")) {
                String currencyName = getNameById(jsonObj.get("currencyCodeA").getAsInt());
                String currencyBuy = jsonObj.get("rateBuy").getAsString();
                String currencySale = jsonObj.get("rateSell").getAsString();
                rateMap.put(currencyName, new NormalizeCurrencyPair(currencyName, currencyBuy, currencySale));
            }
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

    public String getNameById(int code) {
        for (Map.Entry<String, Integer> entry : currencies.entrySet()) {
            if (entry.getValue() == code) {
                return entry.getKey();
            }
        }
        return "EMPTY";
    }

    public int getIdByName(String name) {
        return currencies.getOrDefault(name, -1);
    }

    @Override
    public String toString() {
        return "MonoBank{" +
                "NAME='" + NAME + '\'' +
                ", LOCAL_NAME='" + LOCAL_NAME + '\'' +
                ", API_URL='" + apiUrl + '\'' +
                '}';
    }
}
