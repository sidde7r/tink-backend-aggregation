package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.interfaces.RSAPrivateKey;
import java.util.Base64;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;

public final class JWTUtils {

    private JWTUtils() {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static RSAPrivateKey getKey(String path) {
        return RSA.getPrivateKeyFromBytes(Base64.getDecoder().decode(readFile(path)));
    }

    public static String readFile(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
