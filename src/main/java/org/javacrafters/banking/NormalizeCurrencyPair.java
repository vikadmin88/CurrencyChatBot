package org.javacrafters.banking;


import org.javacrafters.core.AppRegistry;

public class NormalizeCurrencyPair {

    private String name;
    private String buy;
    private String sale;

    {
        name = "N/A";
        buy = "0";
        sale = "0";
    }

    public NormalizeCurrencyPair(String name, String buy, String sale) {
        this.name = name;
        this.buy = buy;
        this.sale = sale;
    }

    public String getName() {
        return name;
    }

    public String getBuy() {
        return buy;
    }

    public String getSale() {
        return sale;
    }

    @Override
    public String toString() {
        return "NormalizeCurrencyPair{" +
                "name='" + name + '\'' +
                ", buy='" + buy + '\'' +
                ", sale='" + sale + '\'' +
                '}';
    }
}
