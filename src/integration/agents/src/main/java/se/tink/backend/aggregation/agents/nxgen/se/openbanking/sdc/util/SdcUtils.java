package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;

public final class SdcUtils {

    public static String getRequestId() {
        return UUID.randomUUID().toString();
    }

    public static String calculateDigest(final String data) {
        return Base64.getEncoder().encodeToString(Hash.sha256(data));
    }

    public static byte[] readFile(final String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (final IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
