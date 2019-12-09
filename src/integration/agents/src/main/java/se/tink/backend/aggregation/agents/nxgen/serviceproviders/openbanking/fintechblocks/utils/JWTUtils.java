package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.Base64;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;

public final class JWTUtils {

    private JWTUtils() {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static PrivateKey getPrivateKey(String path) {
        return RSA.getPrivateKeyFromBytes(Base64.getDecoder().decode(new String(readFile(path))));
    }

    public static byte[] readFile(String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
