package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.security;

import java.security.SecureRandom;
import org.apache.commons.codec.binary.Base64;

public class Token {
    private final byte[] token;

    private Token(int length) {
        byte[] randomBytes = new byte[length];
        new SecureRandom().nextBytes(randomBytes);
        this.token = randomBytes;
    }

    public static Token generate(int length) {
        return new Token(length);
    }

    public static Token generate() {
        return generate(32);
    }

    public byte[] getBytes() {
        return token;
    }

    public String asBase64Encoded() {
        return Base64.encodeBase64String(token);
    }
}
