# ⚙️ Kafka File Log Producer

This project **monitors local log files** and sends new lines to **Kafka topics** in real time.  
Each file is linked to a Kafka topic (for example, `app1.log` → `app1-topic`).

---

## 🧩 How It Works

1. The app reads **`config.json`** to get:
    - Kafka server address (`bootstrapServers`)
    - A list of files and their corresponding Kafka topics
2. It starts a **thread for each file**.
3. Each thread:
    - Watches the file for new lines
    - Sends each new line as a Kafka message to the configured topic

---

## 📁 Example `config.json`

```json
{
  "bootstrapServers": "192.168.60.135:9092",
  "files": [
    { "path": "/home/kafkaproducer/log_file/app1.log", "topic": "app1-topic" },
    { "path": "/home/kafkaproducer/log_file/app2.log", "topic": "app2-topic" },
    { "path": "/home/kafkaproducer/log_file/system.log", "topic": "system-topic" }
  ]
}
```

✅ **To add more files to watch:**  
Add another entry in the `"files"` list like this:
```json
{ "path": "/home/kafkaproducer/log_file/newapp.log", "topic": "newapp-topic" }
```
Restart the producer — it will automatically begin watching the new file and sending logs to the new topic.

---

## 🧰 Requirements

- Java 17 or higher
- Apache Kafka 4.x (broker running)
- Kafka topics already created

---

## ▶️ How to Run

1. **Build the JAR file:**
   ```bash
   mvn clean package
   ```

2. **Run the program:**
   ```bash
   java -jar target/testKafkaProducer-1.0.jar
   ```

You’ll see console logs like:
```
[21:02:45] Watching file: /home/kafkaproducer/log_file/app1.log -> Topic: app1-topic
[21:02:50] Topic: app1-topic Sent message: INFO Application started
```

Press **Ctrl + C** to stop gracefully.

---

## 🧠 Project Structure

```
src/
 └── main/
     ├── java/
     │   └── org/example/
     │       ├── AppMain.java                 # Entry point
     │       ├── config/
     │       │   ├── ConfigLoader.java        # Loads JSON config
     │       │   ├── ConfigData.java          # Config model
     │       │   └── FileItem.java            # One file/topic entry
     │       └── producer/
     │           ├── FileWatcher.java         # Watches files & sends new lines to Kafka
     │           └── KafkaFactory.java        # Creates Kafka producer
     └── resources/
         └── config.json                      # Configuration file
```

---

## 🧩 Class Overview

| Class | Purpose |
|--------|----------|
| `AppMain` | Starts all watchers and manages threads |
| `FileWatcher` | Monitors file changes and sends new lines to Kafka |
| `ConfigLoader` | Reads settings from `config.json` |
| `FileItem` | Represents one file → topic mapping |
| `KafkaFactory` | Creates and configures the Kafka producer |

---

## 💡 Tips

- Keep `config.json` in `src/main/resources` or the same directory as your JAR.
- Each file in `config.json` should exist before running the app.
- You can use this producer together with your **Kafka File Log Consumer** app to complete an end-to-end log streaming pipeline.

---

## 🧑‍💻 Author

**Munycha**  
Kafka Learning Project — Java + Apache Kafka (File Watcher Producer)
