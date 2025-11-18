# 🪵 Kafka File Log Consumer (with Telegram Alerts)

This project listens to Kafka topics, writes each message to a log file, and sends Telegram alerts when messages contain important keywords.  
Each topic runs in its own dedicated thread.

---

## ⚙️ How It Works

1. The application loads settings from config.json (ignored in Git).
2. For each topic in the configuration:
   - A consumer thread starts
   - Messages are written to the specified output file
   - Keywords are checked for alerts
   - If matched, a Telegram notification is sent using TelegramNotifier

---

## 📁 Configuration Files

### ✔ config.example.json (included in Git)

This is the JSON structure your project uses:

    {
      "bootstrapServers": "KAFKA_BOOTSTRAP_SERVER",

      "telegramBotToken": "PUT_YOUR_TELEGRAM_BOT_TOKEN_HERE",
      "telegramChatId": "PUT_YOUR_TELEGRAM_CHAT_ID_HERE",

      "topics": [
        { "topic": "topic-name-1", "output": "/path/to/output1.log" },
        { "topic": "topic-name-2", "output": "/path/to/output2.log" },
        { "topic": "topic-name-3", "output": "/path/to/output3.log" }
      ]
    }

### ✔ How to use it

1. Copy template file:

        cp src/main/resources/config.example.json src/main/resources/config.json

2. Edit your local config.json:
   - Set bootstrapServers
   - Set telegramBotToken
   - Set telegramChatId
   - Set topic/output paths

### ❗ config.json is ignored by Git
This keeps your Telegram token and Chat ID private.

---

## ▶️ How to Get Your Telegram Chat ID

1. Open Telegram
2. Search: @userinfobot
3. Start the bot
4. Copy the Chat ID it gives you
5. Put that into your config.json under telegramChatId

Do **NOT** upload config.json to GitHub.

---

## 🧰 Requirements

- Java 17+
- Apache Kafka broker running
- Kafka topics created
- Internet connection for Telegram alerts

---

## ▶️ Run Instructions

### 1. Build the JAR

    mvn clean package

### 2. Start the consumer

    java -jar target/testKafkaConsumer-1.0.jar

Example output:

    Listening to topic-name-1 -> writing to /path/to/output1.log
    [12:34:56] (topic-name-1) INFO Something happened

---

## 📂 Project Structure

    src/
     └── main/
         ├── java/
         │   └── org/example/
         │       ├── AppMain.java
         │       ├── consumer/
         │       │   └── TopicConsumer.java
         │       ├── config/
         │       │   ├── ConfigLoader.java
         │       │   ├── ConfigData.java
         │       │   └── TopicConfig.java
         │       └── telegram/
         │           └── TelegramNotifier.java
         └── resources/
             ├── config.example.json
             └── config.json   # private, ignored by Git

---

## 🔔 Alert Keyword Detection

TopicConsumer checks for these keywords (case-insensitive):

- error
- fail
- failure
- server error
- 404
- 500

When detected → sends a Telegram alert (non-blocking).

---

## 💡 Tips

- Add as many topics as you want in config.json.
- Each topic runs in its own thread.
- config.json must stay private — Git ignores it.
- This project is for **real-time alerts**, not analytics.
- For analytics later: Kafka → Logstash → Elasticsearch → Kibana.

---

## 🧑‍💻 Author

**Munycha**  
Kafka Learning Project — Multi-Topic Log Consumer + Telegram Alert System
