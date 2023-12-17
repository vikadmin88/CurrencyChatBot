package org.javacrafters.networkclient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class NetworkClientJsoup extends NetworkClient{
@Override
    public String get(String apiUrl) {
    try{
        Document document = Jsoup.connect(apiUrl).ignoreContentType(true).get();
        String responseBody = document.body().text();
        return responseBody;
    }catch (IOException e){
        e.printStackTrace();
        return null;
    }
}

}
