package se.tink.backend.aggregation.agents.framework.wiremock.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ResourceFileReader {

    public String read(String filePath) {
        try {
            return new String(
                    Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
