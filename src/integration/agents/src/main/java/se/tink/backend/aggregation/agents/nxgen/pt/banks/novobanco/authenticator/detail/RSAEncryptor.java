package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.detail;

import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.SecurityConfig.RSA_TRANSFORMATION;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class RSAEncryptor {

    public String encrypt(String data, final Key key) {
        try {
            return encryptRSA(data, key);
        } catch (InvalidKeyException
                | BadPaddingException
                | IllegalBlockSizeException
                | NoSuchPaddingException
                | NoSuchAlgorithmException
                | NoSuchProviderException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private String generateIVWithByteArray(final byte[] array) {
        return new String(Base64.getEncoder().encode(array), StandardCharsets.US_ASCII).trim();
    }

    private String encryptRSA(final String data, final Key key)
            throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException,
                    NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException {
        final Cipher instance =
                Cipher.getInstance(RSA_TRANSFORMATION, BouncyCastleProvider.PROVIDER_NAME);
        instance.init(1, key);
        return generateIVWithByteArray(instance.doFinal(getBytesWithUTF8Charset(data)));
    }

    private byte[] getBytesWithUTF8Charset(final String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }
}
