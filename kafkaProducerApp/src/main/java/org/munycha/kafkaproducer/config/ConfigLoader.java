package org.munycha.kafkaproducer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

/**
 * ConfigLoader is responsible for loading Kafka producer configuration
 * from a JSON file located in the application's classpath.
 *
 * It parses values like Kafka bootstrap servers and the list of files to watch
 * for log data, which are then used to initialize producers and watchers.
 */
public class ConfigLoader {

    // Kafka bootstrap server address
    private final String bootstrapServers;

    // List of files (path + topic) to be watched by producer
    private final List<FileItem> files;

    /**
     * Constructs the ConfigLoader by reading and parsing a JSON config file.
     *
     * @param filePath The name of the JSON configuration file located in resources.
     * @throws Exception If the file is not found or parsing fails.
     */
    public ConfigLoader(String filePath) throws Exception {

        // Load the file as a resource from the classpath
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
        if (inputStream == null) {
            throw new FileNotFoundException("Config file not found in resources: " + filePath);
        }

        // Parse the JSON file into a ConfigData object
        ObjectMapper mapper = new ObjectMapper();
        ConfigData data = mapper.readValue(inputStream, ConfigData.class);

        // Extract required fields from parsed data
        this.bootstrapServers = data.getBootstrapServers();
        this.files = data.getFiles();
    }

    /**
     * @return The Kafka bootstrap servers string (e.g., "localhost:9092")
     */
    public String getBootstrapServers() {
        return bootstrapServers;
    }

    /**
     * @return List of file configuration items (path + topic)
     */
    public List<FileItem> getFiles() {
        return files;
    }
}
