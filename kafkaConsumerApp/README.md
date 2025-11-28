
---

# 🪵 Kafka Log Consumer (Multi-Topic + Telegram Alerts + MySQL Alert Logging)

This project listens to multiple Kafka topics, writes their messages to log files, sends Telegram alerts when important keywords appear, and stores alert events into a MySQL database.

Each topic runs in its own thread for real-time processing.

---

## ⚙️ How It Works

1. The application loads everything from **`config.json`**:

   * Kafka bootstrap servers
   * Telegram bot token
   * Telegram chat ID
   * Topic → output log file mapping
   * Alert keyword list
   * MySQL connection settings

2. For each Kafka topic:

   * A dedicated consumer thread starts
   * Every message is written into a log file
   * Message text is scanned for **alertKeywords**
   * If matched:

      * Send Telegram alert
      * Insert record into MySQL `alert_logs` table

3. All keywords, topics, paths, and DB settings are configured in `config.json`.
   **No Java code changes needed.**

---

## 📁 Configuration Files

### ✔ `config.example.json` (included in Git)

```json
{
  "bootstrapServers": "KAFKA_BOOTSTRAP_SERVER",

  "telegramBotToken": "PUT_YOUR_TELEGRAM_BOT_TOKEN_HERE",
  "telegramChatId": "PUT_YOUR_TELEGRAM_CHAT_ID_HERE",

  "topics": [
    { "topic": "topic-name-1", "output": "/path/to/output1.log" },
    { "topic": "topic-name-2", "output": "/path/to/output2.log" }
  ],

  "alertKeywords": [
    "error",
    "fail",
    "fatal",
    "exception",
    "crash",
    "timeout",
    "warn",
    "500",
    "404"
  ],

  "database": {
    "url": "jdbc:mysql://localhost:3306/logDB",
    "user": "root",
    "password": "YOUR_PASSWORD",
    "table": "alert_logs"
  }
}
```

### ✔ How to use it

```
cp src/main/resources/config.example.json src/main/resources/config.json
```

Edit your new `config.json` with:

* Correct Kafka server
* Bot token & chat ID
* Topic/output paths
* Database settings
* Alert keywords

### ❗ `config.json` is ignored by Git

Your secret credentials stay safe.

---

## 🔔 Extensible Alert Keywords

Alert keywords are fully configurable through `config.json`.

To add new keywords:

```json
"alertKeywords": [
  "error",
  "fail",
  "panic",
  "disconnect",
  "service unavailable",
  "memory leak",
  "unauthorized"
]
```

Restart the app → done.

---

## 🗄 MySQL Alert Logging

Whenever a message matches alert keywords, it is saved into MySQL:

**Table structure:**

```sql
CREATE TABLE alert_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    topic VARCHAR(255) NOT NULL,
    timestamp DATETIME NOT NULL,
    message TEXT NOT NULL
);
```

Fields saved:

* **topic** – Kafka topic
* **timestamp** – message timestamp
* **message** – full original log

---

## ▶️ Getting Your Telegram Chat ID

1. Open Telegram
2. Search: `@userinfobot`
3. Start the bot
4. Copy the chat ID
5. Paste into `config.json`

---

## 🧰 Requirements

* Java 17+
* Apache Kafka running
* Kafka topics created
* MySQL server running
* Internet connection (Telegram API)

---

## ▶️ Run Instructions

### 1. Build the JAR

```
mvn clean package
```

### 2. Run the consumer

```
java -jar target/testKafkaConsumer-1.0.jar
```

Example output:

```
Listening to app1-topic -> writing to /home/.../received_app1.log
[12:34:56] (app1-topic) ERROR Something bad happened
Telegram alert sent.
Alert inserted into database.
```

---

## 📂 Project Structure

```
src/
 └── main/
     ├── java/
     │   └── com/munycha/kafkaconsumer/
     │       ├── AppMain.java
     │       ├── consumer/
     │       │   └── TopicConsumer.java
     │       ├── config/
     │       │   ├── ConfigLoader.java
     │       │   ├── ConfigData.java
     │       │   ├── TopicConfig.java
     │       │   └── DatabaseConfig.java
     │       ├── telegram/
     │       │   └── TelegramNotifier.java
     │       └── db/
     │           └── AlertDatabase.java
     └── resources/
         ├── config.example.json
         └── config.json
```

## 🧩 Class Overview

| Class              | Purpose                                                                                                                         |
| ------------------ | ------------------------------------------------------------------------------------------------------------------------------- |
| `AppMain`          | Loads config, starts all TopicConsumers, creates the shared DB connection, and manages consumer threads                         |
| `TopicConsumer`    | Consumes messages from one Kafka topic, writes logs, detects alert keywords, sends Telegram alerts, and saves alerts into MySQL |
| `ConfigLoader`     | Reads and parses `config.json` into Java objects using Jackson                                                                  |
| `ConfigData`       | Represents the full JSON structure: Kafka config, Telegram config, topics list, alert keywords, and database settings           |
| `TopicConfig`      | Represents a single topic→output mapping defined in `config.json`                                                               |
| `DatabaseConfig`   | Represents MySQL settings: URL, user, password, and table name                                                                  |
| `AlertDatabase`    | Handles MySQL connection and inserts alert records into the alert_logs table                                                    |
| `TelegramNotifier` | Sends formatted alert messages to Telegram with built-in rate limiting                                                          |


---

## 💡 Tips

* Add/remove Kafka topics instantly via `config.json`.
* Add or change alert keywords anytime.
* Runs each topic in parallel to avoid bottlenecks.
* Ideal for **real-time error monitoring**.

### Want analytics or UI?

Use the ELK stack:

```
Kafka → Logstash → Elasticsearch → Kibana
```

---

## 🧑‍💻 Author

**Munycha**
Real-time Kafka Log Consumer + Telegram Alerts + MySQL Logging System

---
