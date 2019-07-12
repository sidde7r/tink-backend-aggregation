package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CmcicUtil {
    private CmcicUtil() {}

    public static byte[] readFile(String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
