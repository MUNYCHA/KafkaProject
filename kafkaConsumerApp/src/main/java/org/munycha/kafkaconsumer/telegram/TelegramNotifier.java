package org.munycha.kafkaconsumer.telegram;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TelegramNotifier {

    private final String botToken;
    private final String chatId;
    private long lastSend = 0;       // Timestamp of last sent message (for rate limiting)

    public TelegramNotifier(String botToken, String chatId) {
        this.botToken = botToken;
        this.chatId = chatId;
    }

    public synchronized void sendMessage(String message) {
        try {
            enforceRateLimit();      // Wait if sending too frequently
            sendRequest(message);    // Send HTTPS request to Telegram API
            lastSend = System.currentTimeMillis();  // Update last sent timestamp
        } catch (Exception e) {
            System.err.println("[TelegramNotifier] Error: " + e.getMessage());
        }
    }


    private void enforceRateLimit() throws InterruptedException {
        long now = System.currentTimeMillis();
        long diff = now - lastSend;

        if (diff < 1000) {
            Thread.sleep(1000 - diff);  // Sleep remaining time to satisfy 1s rate limit
        }
    }


    private URL buildUrl() throws Exception {
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        return new URL(url);
    }


    private void sendRequest(String message) throws Exception {
        URL url = buildUrl();
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

        configureConnection(conn);
        writeRequestBody(conn, message);
        readResponse(conn);
    }


    private void configureConnection(HttpsURLConnection conn) throws Exception {
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(7000);
        conn.setReadTimeout(7000);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
    }


    private void writeRequestBody(HttpsURLConnection conn, String message) throws Exception {
        String body = buildJsonBody(message);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
    }


    private String buildJsonBody(String message) {
        return "{"
                + "\"chat_id\":\"" + chatId + "\","
                + "\"text\":\"" + escapeJson(message) + "\""
                + "}";
    }


    private void readResponse(HttpsURLConnection conn) throws Exception {
        try (InputStream is = conn.getInputStream()) {
        }
    }


    private String escapeJson(String s) {
        return s.replace("\"", "\\\"");
    }
}
