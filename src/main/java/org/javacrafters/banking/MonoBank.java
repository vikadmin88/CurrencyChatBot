package org.javacrafters.banking;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.javacrafters.networkclient.NetworkClient;
import org.javacrafters.utils.ConfigLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonoBank extends Bank {
    NetworkClient netClient;
    private final static String NAME = "Моно-Банк";
    private final static String LOCAL_NAME = "MB";
//    private final static String API_URL = "https://api.monobank.ua/bank/currency";
private final static String API_URL = ConfigLoader.get("MB_API_URL");

    private final static Map<String, Integer> currencies = new HashMap<>();
    static {
        currencies.put("UAH", 980);
        currencies.put("USD", 840);
        currencies.put("EUR", 978);
        currencies.put("GBP", 826);
        currencies.put("PLN", 985);
    }
    public MonoBank() {
    }

    public MonoBank(NetworkClient netClient) {
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

    @Override
    public void setNetClient(NetworkClient netClient) {
        this.netClient = netClient;
    }

    public static String getNameById(int code) {
        for (Map.Entry<String, Integer> entry : currencies.entrySet()) {
            if (entry.getValue() == code) {
                return entry.getKey();
            }
        }
        return "EMPTY";
    }

    public static int getIdByName(String name) {
        return currencies.getOrDefault(name, -1);
    }

    @Override
    public String toString() {
        return "MonoBank{" +
                "NAME='" + NAME + '\'' +
                ", LOCAL_NAME='" + LOCAL_NAME + '\'' +
                '}';
    }
}
