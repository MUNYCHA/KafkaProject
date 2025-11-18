package org.example.telegram;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class TelegramNotifier {
    private final String botToken;
    private final String chatId;

    public TelegramNotifier(String botToken, String chatId) {
        this.botToken = botToken;
        this.chatId = chatId;
    }

    public void sendMessage(String message) {
        try {
            String encoded = URLEncoder.encode(message, "UTF-8");
            String urlString = "https://api.telegram.org/bot" + botToken +
                    "/sendMessage?chat_id=" + chatId +
                    "&text=" + encoded;

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.getInputStream().close(); // execute request
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
