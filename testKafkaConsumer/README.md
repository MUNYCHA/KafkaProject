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
      { "topic": "app1-topic", "output": "D:/log_file/received_app1.log" },
      { "topic": "app2-topic", "output": "D:/log_file/received_app2.log" },
      { "topic": "app3-topic", "output": "D:/log_file/received_app3.log" },
      { "topic": "app4-topic", "output": "D:/log_file/received_app4.log" },
      { "topic": "system-topic", "output": "D:/log_file/received_system.log" }
   ]
}
```

✅ **To consume more topics:**  
Add another object inside the `"topics"` list:

```json
{ "topic": "new-topic", "output": "D:/log_file/new.log" }
```

Then restart the program — it will automatically start listening to that new topic.

---

## 🧰 Requirements

- Java 17 or higher
- Apache Kafka running (broker reachable)
- Kafka topics already created

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
| `ConfigLoader` | Reads settings from `config.json` |
| `TopicConfig` | Holds topic name and output file path info |

---

## 💡 Tip

Keep your Kafka broker address and log paths correct in `config.json`.  
You can use this same consumer with your **Kafka File Log Producer** project.

---

## 🧑‍💻 Author

**Munycha**  
Kafka Learning Project — Java + Apache Kafka (Multi-threaded Log Consumer)
