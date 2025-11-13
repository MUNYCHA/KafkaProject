# 🪵 Kafka File Log Consumer

This project listens to **Kafka topics** and writes every message to a **log file**.  
Each topic has its own output file.

---

## ⚙️ How It Works

1. The app reads **`config.json`** for:
    - Kafka server address (`bootstrapServers`)
    - List of topics and log file paths
2. For each topic, it starts a thread that:
    - Connects to Kafka
    - Reads new messages
    - Writes them into the log file

---

## 📁 Example `config.json`

```json
{
  "bootstrapServers": "192.168.60.135:9092",
  "topics": [
    { "topic": "app1-topic", "output": "/home/kafkaconsumer/logs/app1.log" },
    { "topic": "app2-topic", "output": "/home/kafkaconsumer/logs/app2.log" }
  ]
}
```

✅ **To consume more topics:**  
Add another object inside the `"topics"` list:

```json
{ "topic": "new-topic", "output": "/home/kafkaconsumer/logs/new.log" }
```

Then restart the program — it will automatically start listening to that new topic.

---

## 🧰 Requirements

- Java 17 or higher
- Apache Kafka running (broker reachable)
- Kafka topics already created

---

## ▶️ How to Run

1. **Build the JAR file:**
   ```bash
   mvn clean package
   ```

2. **Run the program:**
   ```bash
   java -jar target/testKafkaConsumer-1.0.jar
   ```

You’ll see logs like:
```
Listening to app1-topic -> writing to /home/kafkaconsumer/logs/app1.log
[20:45:33] (app1-topic) INFO Application started
```

Press **Ctrl + C** to stop safely.

---

## 📂 Project Structure

```
src/
 └── main/
     ├── java/
     │   └── org/example/
     │       ├── AppMain.java                # Entry point
     │       ├── consumer/
     │       │   └── TopicConsumer.java      # Kafka consumer class
     │       └── config/
     │           ├── ConfigLoader.java       # Loads JSON config
     │           ├── ConfigData.java         # Config model
     │           └── TopicConfig.java        # Represents one topic config
     └── resources/
         └── config.json                     # Configuration file
```

---

## 🧠 Class Overview

| Class | Purpose |
|-------|----------|
| `AppMain` | Starts the consumer threads for each topic |
| `TopicConsumer` | Consumes messages and writes them to files |
| `ConfigLoader` | Reads and parses settings from `config.json` |
| `ConfigData` | Represents the full JSON structure (bootstrap servers and list of TopicConfig objects) |
| `TopicConfig` | Holds topic name and output file path info |


---

## 💡 Tip

Keep your Kafka broker address and log paths correct in `config.json`.  
You can use this same consumer with your **Kafka File Log Producer** project.

---

## 🧑‍💻 Author

**Munycha**  
Kafka Learning Project — Java + Apache Kafka (Multi-threaded Log Consumer)
