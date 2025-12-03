package org.munycha.kafkaconsumer.telegram;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Sends alert messages to a specified Telegram chat using Telegram Bot API.
 * Implements basic rate-limiting to avoid flooding the API.
 */
public class TelegramNotifier {

    private final String botToken;   // Telegram bot token
    private final String chatId;     // Target chat ID for sending messages
    private long lastSend = 0;       // Timestamp of last sent message (for rate limiting)

    /**
     * Constructs a new TelegramNotifier with bot credentials.
     *
     * @param botToken Telegram bot token
     * @param chatId   Telegram chat ID
     */
    public TelegramNotifier(String botToken, String chatId) {
        this.botToken = botToken;
        this.chatId = chatId;
    }

    /**
     * Sends a message to the configured Telegram chat, with built-in rate-limiting (1 msg/sec).
     *
     * @param message The message text to send
     */
    public synchronized void sendMessage(String message) {
        try {
            enforceRateLimit();      // Wait if sending too frequently
            sendRequest(message);    // Send HTTPS request to Telegram API
            lastSend = System.currentTimeMillis();  // Update last sent timestamp
        } catch (Exception e) {
            System.err.println("[TelegramNotifier] Error: " + e.getMessage());
        }
    }

    /**
     * Enforces a 1-second delay between messages to prevent flooding.
     */
    private void enforceRateLimit() throws InterruptedException {
        long now = System.currentTimeMillis();
        long diff = now - lastSend;

        if (diff < 1000) {
            Thread.sleep(1000 - diff);  // Sleep remaining time to satisfy 1s rate limit
        }
    }

    /**
     * Constructs the Telegram API URL for sending messages.
     *
     * @return Fully formed API URL
     */
    private URL buildUrl() throws Exception {
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        return new URL(url);
    }

    /**
     * Sends the full HTTPS POST request with the message body to Telegram.
     *
     * @param message The message content
     */
    private void sendRequest(String message) throws Exception {
        URL url = buildUrl();
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

        configureConnection(conn);
        writeRequestBody(conn, message);
        readResponse(conn);
    }

    /**
     * Configures basic connection parameters for the POST request.
     */
    private void configureConnection(HttpsURLConnection conn) throws Exception {
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(7000);
        conn.setReadTimeout(7000);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
    }

    /**
     * Writes the formatted JSON payload to the HTTPS request body.
     *
     * @param conn    The HTTPS connection
     * @param message The message to send
     */
    private void writeRequestBody(HttpsURLConnection conn, String message) throws Exception {
        String body = buildJsonBody(message);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Builds the JSON payload body required by the Telegram API.
     *
     * @param message The message text
     * @return JSON-formatted string
     */
    private String buildJsonBody(String message) {
        return "{"
                + "\"chat_id\":\"" + chatId + "\","
                + "\"text\":\"" + escapeJson(message) + "\""
                + "}";
    }

    /**
     * Reads the response from Telegram. Does nothing if successful.
     */
    private void readResponse(HttpsURLConnection conn) throws Exception {
        try (InputStream is = conn.getInputStream()) {
            // No-op: success if no exception is thrown
        }
    }

    /**
     * Escapes double quotes in the message to avoid malformed JSON.
     *
     * @param s The original message
     * @return Escaped message
     */
    private String escapeJson(String s) {
        return s.replace("\"", "\\\"");
    }
}
