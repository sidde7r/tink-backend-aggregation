package se.tink.backend.aggregation.agents.framework.wiremock.utils;

import static java.nio.charset.StandardCharsets.UTF_8;
import static lombok.AccessLevel.PRIVATE;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor(access = PRIVATE)
public final class ResourceFileReader {

    @SneakyThrows
    public static String read(String filePath) {
        return read(Paths.get(filePath).toFile());
    }

    @SneakyThrows
    public static String read(File file) {
        return new String(Files.readAllBytes(file.toPath()), UTF_8.name());
    }
}
