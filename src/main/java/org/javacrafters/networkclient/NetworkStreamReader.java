package org.javacrafters.networkclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class NetworkStreamReader extends NetworkClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkStreamReader.class);

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
            LOGGER.error("NetworkStreamReader {}", e);
        }
        return url;
    }
}