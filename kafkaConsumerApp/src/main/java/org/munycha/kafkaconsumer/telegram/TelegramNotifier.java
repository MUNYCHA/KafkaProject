package org.munycha.kafkaconsumer.telegram;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TelegramNotifier {

    private final String botToken;
    private final String chatId;

    // Global rate limit timestamp (1 msg/sec)
    private long lastSend = 0;

    public TelegramNotifier(String botToken, String chatId) {
        this.botToken = botToken;
        this.chatId = chatId;
    }

    // ------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------
    public synchronized void sendMessage(String message) {
        try {
            enforceRateLimit();
            sendRequest(message);
            lastSend = System.currentTimeMillis();

        } catch (Exception e) {
            System.err.println("[TelegramNotifier] Error: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------
    // 1. Rate limit logic (cleaner, isolated)
    // ------------------------------------------------------------
    private void enforceRateLimit() throws InterruptedException {
        long now = System.currentTimeMillis();
        long diff = now - lastSend;

        if (diff < 1000) {
            Thread.sleep(1000 - diff);
        }
    }

    // ------------------------------------------------------------
    // 2. Build Telegram URL
    // ------------------------------------------------------------
    private URL buildUrl() throws Exception {
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        return new URL(url);
    }

    // ------------------------------------------------------------
    // 3. Send HTTPS POST request
    // ------------------------------------------------------------
    private void sendRequest(String message) throws Exception {
        URL url = buildUrl();
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

        configureConnection(conn);
        writeRequestBody(conn, message);
        readResponse(conn);
    }

    // ------------------------------------------------------------
    // 4. Configure POST connection
    // ------------------------------------------------------------
    private void configureConnection(HttpsURLConnection conn) throws Exception {
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(7000);
        conn.setReadTimeout(7000);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
    }

    // ------------------------------------------------------------
    // 5. Build + send JSON body
    // ------------------------------------------------------------
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

    // ------------------------------------------------------------
    // 6. Read response to ensure success
    // ------------------------------------------------------------
    private void readResponse(HttpsURLConnection conn) throws Exception {
        try (InputStream is = conn.getInputStream()) {
            // No processing needed — success if no exception
        }
    }

    // ------------------------------------------------------------
    // 7. Escape JSON special characters
    // ------------------------------------------------------------
    private String escapeJson(String s) {
        return s.replace("\"", "\\\"");
    }
}
