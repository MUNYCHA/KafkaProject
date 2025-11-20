package org.example.telegram;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TelegramNotifier {

    private final String botToken;
    private final String chatId;

    // Global rate limit timestamp
    private long lastSend = 0;

    public TelegramNotifier(String botToken, String chatId) {
        this.botToken = botToken;
        this.chatId = chatId;
    }

    public synchronized void sendMessage(String message) {
        try {
            // --- RATE LIMIT PROTECTION ---
            long now = System.currentTimeMillis();
            long diff = now - lastSend;

            // Telegram allows 1 message per second safely
            if (diff < 1000) {
                Thread.sleep(1000 - diff);
            }

            String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());

            String urlStr = "https://api.telegram.org/bot" + botToken +
                    "/sendMessage?chat_id=" + chatId +
                    "&text=" + encoded;

            URL url = new URL(urlStr);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setConnectTimeout(7000);
            conn.setReadTimeout(7000);

            try (InputStream is = conn.getInputStream()) {
                // success, nothing else needed
            }

            // update timestamp AFTER success
            lastSend = System.currentTimeMillis();

        } catch (Exception e) {
            System.err.println("[TelegramNotifier] Failed: " + e.getMessage());
        }
    }
}
