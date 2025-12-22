package org.munycha.kafkaproducer.producer;

import org.munycha.kafkaproducer.model.PathStorage;

import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class StorageCollector {

    public static List<PathStorage> collect(List<String> paths) {
        List<PathStorage> result = new ArrayList<>();

        for (String p : paths) {
            try {
                Path path = Paths.get(p);
                if (!Files.exists(path)) {
                    continue;
                }

                FileStore store = Files.getFileStore(path);

                long total = store.getTotalSpace();
                long usable = store.getUsableSpace();
                long used = total - usable;
                double usedPercent = total > 0
                        ? (double) used * 100.0 / total
                        : 0.0;

                PathStorage ps = new PathStorage();
                ps.setPath(p);
                ps.setTotalBytes(total);
                ps.setUsedBytes(used);
                ps.setUsedPercent(usedPercent);

                result.add(ps);

            } catch (Exception ignored) {
            }
        }
        return result;
    }
}


