package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.security.PublicKey;
import se.tink.backend.aggregation.agents.utils.crypto.EllipticCurve;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

// According to rfc7517

@JsonObject
public class JsonWebKey {
    @JsonProperty("kty")
    private String keyType;

    private String use;

    @JsonProperty("kid")
    private String keyId;

    @JsonProperty("n")
    private String modulus;

    @JsonProperty("e")
    private String exponent;

    @JsonProperty("x")
    private String ecXPoint;

    @JsonProperty("y")
    private String ecYPoint;

    @JsonProperty("crv")
    private String curve;

    @JsonIgnore
    public String getKeyId() {
        return keyId;
    }

    @JsonIgnore
    public boolean isSigningKey() {
        return "sig".equalsIgnoreCase(use);
    }

    @JsonIgnore
    private boolean isRsaKey() {
        return "rsa".equalsIgnoreCase(keyType);
    }

    @JsonIgnore
    private boolean isEcKey() {
        return "ec".equalsIgnoreCase(keyType);
    }

    @JsonIgnore
    private PublicKey createRsaKey() {
        if (Strings.isNullOrEmpty(modulus) || Strings.isNullOrEmpty(exponent)) {
            throw new IllegalStateException(String.format("RSA NYI, kid: %s", keyId));
        }

        byte[] modulusBytes = EncodingUtils.decodeBase64String(modulus);
        byte[] exponentBytes = EncodingUtils.decodeBase64String(exponent);
        return RSA.getPublicKeyFromModulusAndExponent(modulusBytes, exponentBytes);
    }

    @JsonIgnore
    private PublicKey createEcKey() {
        if (Strings.isNullOrEmpty(ecXPoint)
                || Strings.isNullOrEmpty(ecYPoint)
                || Strings.isNullOrEmpty(curve)) {
            throw new IllegalStateException(String.format("Invalid EC key: %s", keyId));
        }

        byte[] xPointBytes = EncodingUtils.decodeBase64String(ecXPoint);
        byte[] yPointBytes = EncodingUtils.decodeBase64String(ecYPoint);
        return EllipticCurve.getPublicKeyFromCurveAndPoints(curve, xPointBytes, yPointBytes);
    }

    @JsonIgnore
    public PublicKey getPublicKey() {
        if (isRsaKey()) {
            return createRsaKey();
        } else if (isEcKey()) {
            return createEcKey();
        } else {
            throw new IllegalStateException(String.format("Unknown key type: %s", keyType));
        }
    }
}
