package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class PaymentDigester {
    private final MessageDigest messageDigest;
    private final Base64.Encoder encoder = Base64.getEncoder();

    public PaymentDigester() {
        try {
            this.messageDigest = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException();
        }
    }

    public String digest(Object object) {
        return Optional.ofNullable(SerializationUtils.serializeToString(object))
                .map(String::getBytes)
                .map(this::digest)
                .map(encoder::encodeToString)
                .orElse("");
    }

    private byte[] digest(byte[] bytes) {
        for (byte b : bytes) {
            messageDigest.update(b);
        }
        return messageDigest.digest();
    }
}
