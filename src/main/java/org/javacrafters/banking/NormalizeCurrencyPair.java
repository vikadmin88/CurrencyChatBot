package org.javacrafters.banking;

public class NormalizeCurrencyPair {

    private String name;
    private String buy;
    private String sale;
    private String cross;



    public NormalizeCurrencyPair(String name, String buy, String sale, String cross) {
        this.name = name;
        this.buy = buy;
        this.sale = sale;
        this.cross = cross;
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
    public String getCross() {
        return cross;
    }

    @Override
    public String toString() {
        return "NormalizeCurrencyPair{" +
                "name='" + name + '\'' +
                ", buy='" + buy + '\'' +
                ", sale='" + sale + '\'' +
                ", cross='" + cross + '\'' +
                '}';
    }
}