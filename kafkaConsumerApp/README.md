
# 🪵 Kafka File Log Consumer (with Extensible Telegram Alerts)

This project listens to multiple Kafka topics, writes their messages to log files, and sends Telegram alerts whenever incoming messages contain **configurable alert keywords**.

Each Kafka topic runs in its own independent thread for parallel processing and real-time responsiveness.

---

## ⚙️ How It Works

1. The application loads **all settings from `config.json`**, including:

   * Kafka bootstrap servers
   * Telegram bot token
   * Telegram chat ID
   * Topic → output file mappings
   * Alert keyword list

2. For each defined topic:

   * A dedicated consumer thread starts
   * Messages are appended to a log file
   * Message text is checked against **keywords from config.json**
   * If a keyword matches → TelegramNotifier sends an alert

3. All alert keywords are controlled entirely through config.json, making it easy to add/remove keywords without modifying Java code.

---

## 📁 Configuration Files

### ✔ `config.example.json` (included in Git)

Template structure:

```json
{
  "bootstrapServers": "KAFKA_BOOTSTRAP_SERVER",

  "telegramBotToken": "PUT_YOUR_TELEGRAM_BOT_TOKEN_HERE",
  "telegramChatId": "PUT_YOUR_TELEGRAM_CHAT_ID_HERE",

  "topics": [
    { "topic": "topic-name-1", "output": "/path/to/output1.log" },
    { "topic": "topic-name-2", "output": "/path/to/output2.log" },
    { "topic": "topic-name-3", "output": "/path/to/output3.log" }
  ],

  "alertKeywords": [
    "error",
    "fail",
    "exception",
    "fatal",
    "timeout",
    "warn",
    "critical",
    "500",
    "404"
  ]
}
```

### ✔ How to use it

1. Copy the example file:

```
cp src/main/resources/config.example.json src/main/resources/config.json
```

2. Edit your local `config.json`:

   * Set your Kafka bootstrap servers
   * Add your Telegram bot token
   * Add your Telegram chat ID
   * Add topic/output file entries
   * Add/modify alert keywords

### ❗ `config.json` is ignored by Git

Your secrets (Telegram token, chat ID) stay private.

---

## 🔔 Extensible Alert Keywords

Alert keywords are **no longer hard-coded**.
You fully control them through `config.json`.

### Examples of supported keywords:

* `"error"`
* `"timeout"`
* `"fatal"`
* `"warn"`
* `"exception"`
* `"404"`
* `"500"`

### 💡 Want to add more?

Just edit `alertKeywords`:

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

Save → restart the app → done.
**No Java code changes needed.**

This design makes the system highly flexible and easy to maintain.

---

## ▶️ How to Get Your Telegram Chat ID

1. Open Telegram
2. Search: `@userinfobot`
3. Start the bot
4. Copy the Chat ID
5. Paste into `config.json`

---

## 🧰 Requirements

* Java 17+
* A running Kafka broker
* Existing Kafka topics
* Internet access for Telegram API

---

## ▶️ Run Instructions

### 1. Build the JAR:

```
mvn clean package
```

### 2. Run the consumer:

```
java -jar target/testKafkaConsumer-1.0.jar
```

Example output:

```
Listening to app1-topic -> writing to /path/to/output.log
[12:34:56] (app1-topic) ERROR Server failed to start
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
     │       │   └── TopicConfig.java
     │       ├── telegram/
     │       │   └── TelegramNotifier.java
     │       └── db/
     │           └── AlertDatabase.java
     └── resources/
         ├── config.example.json
         └── config.json

```

---

## 💡 Tips

* Add or remove topics easily in the config.
* Add as many alert keywords as needed (no Java code change).
* Each topic runs independently, preventing bottlenecks.
* This system is ideal for **real-time production log monitoring**.
* For analytics or visualization, consider:

   * Kafka → Logstash → Elasticsearch → Kibana (ELK Stack)

---

## 🧑‍💻 Author

**Munycha**
Kafka Multi-Topic Log Consumer + Flexible Telegram Alerting System

---

