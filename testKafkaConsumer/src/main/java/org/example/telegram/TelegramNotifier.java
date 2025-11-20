package org.example.telegram;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
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

            // Telegram API endpoint
            String urlStr = "https://api.telegram.org/bot" + botToken + "/sendMessage";

            URL url = new URL(urlStr);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            // ---- POST SETTINGS ----
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(7000);
            conn.setReadTimeout(7000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            // ---- REQUEST BODY ----
            String jsonBody = "{"
                    + "\"chat_id\":\"" + chatId + "\","
                    + "\"text\":\"" + escapeJson(message) + "\""
                    + "}";

            // ---- SEND BODY ----
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }

            // ---- READ RESPONSE ----
            try (InputStream is = conn.getInputStream()) {
                // success - nothing needed
            }

            // update timestamp AFTER success
            lastSend = System.currentTimeMillis();

        } catch (Exception e) {
            System.err.println("[TelegramNotifier] Failed: " + e.getMessage());
        }
    }

    // Escape quotes for JSON safety
    private String escapeJson(String s) {
        return s.replace("\"", "\\\"");
    }
}
