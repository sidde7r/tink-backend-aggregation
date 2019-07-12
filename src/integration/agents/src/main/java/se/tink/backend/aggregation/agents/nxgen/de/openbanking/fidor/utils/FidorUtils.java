package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class FidorUtils {

    public static byte[] readFile(String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
