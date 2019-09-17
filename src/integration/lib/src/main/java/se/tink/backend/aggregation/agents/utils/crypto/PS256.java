package se.tink.backend.aggregation.agents.utils.crypto;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import java.security.interfaces.RSAPrivateKey;
import java.util.Arrays;
import net.minidev.json.JSONObject;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class PS256 {
    private PS256() {}

    public static String sign(JWSHeader header, byte[] payloadObject, RSAPrivateKey signingKey) {
        Payload payload = new Payload(payloadObject);
        return sign(header, signingKey, payload, Boolean.FALSE);
    }

    public static String sign(JWSHeader header, String payloadObject, RSAPrivateKey signingKey) {
        Payload payload = new Payload(payloadObject);
        return sign(header, signingKey, payload, Boolean.FALSE);
    }

    public static String sign(
            JWSHeader header, JSONObject payloadObject, RSAPrivateKey signingKey) {
        Payload payload = new Payload(SerializationUtils.serializeToString(payloadObject));
        return sign(header, signingKey, payload, Boolean.FALSE);
    }

    public static String sign(
            JWSHeader header,
            JSONObject payloadObject,
            RSAPrivateKey signingKey,
            Boolean detatched) {
        Payload payload = new Payload(SerializationUtils.serializeToString(payloadObject));
        return sign(header, signingKey, payload, detatched);
    }

    private static String sign(
            JWSHeader header, RSAPrivateKey signingKey, Payload payload, Boolean detatchedPayload) {
        JWSObject signed = new JWSObject(header, payload);
        JWSSigner signer = new RSASSASigner(signingKey);
        try {
            signed.sign(signer);

        } catch (JOSEException e) {
            throw new IllegalStateException(
                    "Signing request with PS256 failed." + Arrays.toString(e.getStackTrace()));
        }
        return detatchedPayload.equals(Boolean.TRUE) ? signed.serialize(true) : signed.serialize();
    }
}
