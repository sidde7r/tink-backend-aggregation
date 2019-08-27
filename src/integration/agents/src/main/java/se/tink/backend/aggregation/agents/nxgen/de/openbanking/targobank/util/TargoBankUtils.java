package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class TargoBankUtils {

    public static byte[] readFile(final String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (final IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private TargoBankUtils() {
        throw new AssertionError();
    }
}
