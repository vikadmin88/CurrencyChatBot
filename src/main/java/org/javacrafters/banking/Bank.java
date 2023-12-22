package org.javacrafters.banking;

import java.util.Map;

public abstract class Bank {

    public abstract Map<String, NormalizeCurrencyPair> getRates();

    public abstract String getName();

    public abstract String getLocalName();

}