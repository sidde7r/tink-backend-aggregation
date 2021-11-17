package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.macgenerator;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import se.tink.libraries.cryptography.hash.Hash;

public class MacSignatureCreator {

    private static final String NONCE_SUFFIX = ":AMEX";

    String createSignature(String secretKey, String baseString) {
        return Hash.hmacSha256AsBase64(
                secretKey.getBytes(StandardCharsets.UTF_8),
                baseString.getBytes(StandardCharsets.UTF_8));
    }

    String createNonce() {
        return UUID.randomUUID() + NONCE_SUFFIX;
    }
}
