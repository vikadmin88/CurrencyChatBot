package org.javacrafters.banking;

import org.javacrafters.networkclient.NetworkClient;
import java.util.Map;

public abstract class Bank {

public abstract Map<String, NormalizeCurrencyPair> getRates();
public abstract String getName();
public abstract String getLocalName();
public abstract void setNetClient(NetworkClient client);

}
