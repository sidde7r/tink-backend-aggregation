package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.detail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants;

public class DigestCalc {

    private static final String ALGORITHM = "HmacSHA256";

    public String calculateRequestDigest(Object payload) {
        try {
            return calculateRequestDigest(toJson(payload).getBytes());
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private String calculateRequestDigest(final byte[] array)
            throws InvalidKeyException, NoSuchAlgorithmException {
        final String instanceKey = NovoBancoConstants.Secrets.INSTANCE_KEY;
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
            throws NoSuchAlgorithmException, InvalidKeyException {
        final Mac instance = Mac.getInstance(ALGORITHM);
        instance.init(new SecretKeySpec(key, ALGORITHM));
        return generateIVWithByteArray(instance.doFinal(dataToEncrypt));
    }

    private String generateIVWithByteArray(final byte[] array) {
        return Base64.getEncoder().encodeToString(array);
    }
}
