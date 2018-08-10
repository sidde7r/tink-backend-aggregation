package se.tink.libraries.cryptography;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class RSAUtilsTest {
    private static final String PRIVATE_KEY_PATH = "data/test/cryptography/private_rsa_key.pem";
    private static final String PUBLIC_KEY_PATH = "data/test/cryptography/public_rsa_key.pub";

    @Test
    public void testReadPrivateKey() throws IOException {
        RSAPrivateKey key = RSAUtils.getPrivateKey(PRIVATE_KEY_PATH);

        assertThat(key).isNotNull();
    }

    @Test
    public void testReadPublicKey() throws IOException {
        RSAPublicKey key = RSAUtils.getPublicKey(PUBLIC_KEY_PATH);

        assertThat(key).isNotNull();
    }

    @Test
    public void testSignAndVerify() throws Exception {
        byte[] data = "data-to-sign".getBytes();

        Signature signature = RSAUtils.getSignature(RSAUtils.getPrivateKey(PRIVATE_KEY_PATH));
        signature.update(data);

        byte[] signedBytes = signature.sign();

        signature = RSAUtils.getSignature(RSAUtils.getPublicKey(PUBLIC_KEY_PATH));
        signature.update(data);

        assertThat(signature.verify(signedBytes)).isTrue();
    }
}
