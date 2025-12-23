package org.munycha.kafkaconsumer.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;

public class ConfigLoader {

    private final String filePath;

    public ConfigLoader(String filePath) {
        this.filePath = filePath;
    }

    public AppConfig load() throws IOException {

        try (InputStream inputStream = loadConfigFile()) {

            if (inputStream == null) {
                throw new FileNotFoundException(
                        "Config file not found (external or classpath): " + filePath
                );
            }

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(inputStream, AppConfig.class);
        }
    }

    private InputStream loadConfigFile() throws FileNotFoundException {

        File externalFile = new File(filePath);

        if (externalFile.exists()) {
            System.out.println(
                    "[ConfigLoader] Loading EXTERNAL config: " + externalFile.getAbsolutePath()
            );
            return new FileInputStream(externalFile);
        }

        System.out.println(
                "[ConfigLoader] External config not found. Trying INTERNAL resource: " + filePath
        );

        return getClass()
                .getClassLoader()
                .getResourceAsStream(filePath);
    }
}
