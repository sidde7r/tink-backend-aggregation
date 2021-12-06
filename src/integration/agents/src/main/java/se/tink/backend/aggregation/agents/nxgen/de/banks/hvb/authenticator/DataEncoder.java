package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator;

import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.util.Base64;
import java.util.Optional;
import se.tink.libraries.cryptography.RSA;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DataEncoder {

    String base64Encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().encodeToString(data);
    }

    String rsaSha256SignBase64Encode(RSAPrivateKey rsaPrivateKey, String dataToSign) {
        return base64UrlEncode(
                rsaSha256Sign(rsaPrivateKey, dataToSign.getBytes(StandardCharsets.UTF_8)));
    }

    String serializeAndBase64(Object object) {
        return Optional.of(object)
                .map(SerializationUtils::serializeToString)
                .map(String::getBytes)
                .map(this::base64Encode)
                .orElseThrow(() -> new IllegalArgumentException("Couldn't serialize object."));
    }

    private byte[] rsaSha256Sign(RSAPrivateKey rsaPrivateKey, byte[] dataToSign) {
        return RSA.signSha256(rsaPrivateKey, dataToSign);
    }
}
