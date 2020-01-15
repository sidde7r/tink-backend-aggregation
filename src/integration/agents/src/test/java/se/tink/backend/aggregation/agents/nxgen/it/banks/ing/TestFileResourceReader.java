package se.tink.backend.aggregation.agents.nxgen.it.banks.ing;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TestFileResourceReader {

    private final String modulePath;

    String readFileContent(String resourcePath) {
        try {
            return new String(
                    Files.readAllBytes(Paths.get(finalFilePath(resourcePath))),
                    StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String finalFilePath(String resourcePath) {
        return String.format("%s/%s", modulePath, resourcePath);
    }
}
