package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigLoader {
    private final String bootstrapServers;
    private final JsonNode filesNode;

    public ConfigLoader(String filePath) throws Exception {
        String configContent = new String(Files.readAllBytes(Paths.get(filePath)));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(configContent);

        this.bootstrapServers = root.get("bootstrapServers").asText();
        this.filesNode = root.get("files"); // array of { "path": "...", "topic": "..." }
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public JsonNode getFilesNode() {
        return filesNode;
    }
}
