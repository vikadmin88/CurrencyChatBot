package org.javacrafters.networkclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class NetworkStreamReader extends NetworkClient {
    @Override
   public String get(String url) {

        try (InputStream is = new URL(url).openStream()) {

            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();

            int cp;
            while ((cp = br.read()) != -1) {
                sb.append((char) cp);
            }
            return sb.toString();

        } catch (IOException e) {
            e.printStackTrace();
            return "[]";
        }
    }

}
