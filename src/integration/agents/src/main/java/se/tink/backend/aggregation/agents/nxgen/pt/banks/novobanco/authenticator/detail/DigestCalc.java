package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.detail;

import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.SecurityConfig.HMAC_SHA256;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants;

public class DigestCalc {
    public String calculateRequestDigest(Object payload) {
        try {
            return calculateRequestDigest(toJson(payload).getBytes());
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private String calculateRequestDigest(final byte[] array)
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException {
        final String instanceKey = NovoBancoConstants.SecretKeys.INSTANCE_KEY;
        return encryptHmacSHA256(Base64.getDecoder().decode(instanceKey), array);
    }

    private String toJson(Object payload) {
        ObjectMapper om = new ObjectMapper();
        try {
            return om.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private String encryptHmacSHA256(final byte[] key, final byte[] dataToEncrypt)
            throws NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException {
        final Mac instance = Mac.getInstance(HMAC_SHA256, BouncyCastleProvider.PROVIDER_NAME);
        instance.init(new SecretKeySpec(key, HMAC_SHA256));
        return generateIVWithByteArray(instance.doFinal(dataToEncrypt));
    }

    private String generateIVWithByteArray(final byte[] array) {
        return Base64.getEncoder().encodeToString(array);
    }
}
