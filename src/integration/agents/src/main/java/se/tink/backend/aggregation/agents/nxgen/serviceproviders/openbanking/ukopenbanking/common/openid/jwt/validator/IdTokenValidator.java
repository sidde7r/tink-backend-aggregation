package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.validator;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Slf4j
public class IdTokenValidator {

    private final String idToken;

    private final Map<String, PublicKey> publicKeys;

    private String accessToken;

    private String code;

    private String state;

    private boolean atHashValidation;

    private boolean cHashValidation;

    private boolean sHashValidation;

    private ValidatorMode mode = ValidatorMode.TERMINATING;

    public IdTokenValidator(String idToken, Map<String, PublicKey> publicKeys) {
        this.idToken = idToken;
        this.publicKeys = publicKeys;
    }

    public IdTokenValidator withAtHashValidation(String accessToken) {
        this.accessToken = accessToken;
        this.atHashValidation = true;
        return this;
    }

    public IdTokenValidator withCHashValidation(String code) {
        this.code = code;
        this.cHashValidation = true;
        return this;
    }

    public IdTokenValidator withSHashValidation(String state) {
        this.state = state;
        this.sHashValidation = true;
        return this;
    }

    public IdTokenValidator withMode(ValidatorMode mode) {
        this.mode = mode;
        return this;
    }

    public boolean execute() {
        String[] parts = idToken.split("\\.");

        if (parts.length != 3) {
            handleError("Invalid format");
            return false;
        }

        IdTokenHeader header =
                SerializationUtils.deserializeFromBytes(
                        EncodingUtils.decodeBase64String(parts[0]), IdTokenHeader.class);

        IdTokenPayload payload =
                SerializationUtils.deserializeFromBytes(
                        EncodingUtils.decodeBase64String(parts[1]), IdTokenPayload.class);

        try {
            if (!validateSignature(header.getKid(), header.getAlg(), parts)) {
                handleError(
                        "Invalid signature (alg:"
                                + header.getAlg()
                                + ", kid:"
                                + header.getKid()
                                + ")");
                return false;
            }
        } catch (Exception ex) {
            /* any exception has to be caught in case of logging mode */
            handleError(ex.getMessage(), ex);
            return false;
        }

        if (atHashValidation) {
            guardValidateHash(accessToken, header.getAlg(), payload.getAtHash(), "Invalid at_hash");
        }

        if (cHashValidation) {
            guardValidateHash(code, header.getAlg(), payload.getCHash(), "Invalid c_hash");
        }

        if (sHashValidation) {
            guardValidateHash(state, header.getAlg(), payload.getSHash(), "Invalid s_hash");
        }

        return true;
    }

    private void guardValidateHash(String source, String alg, String hash, String errorMessage) {
        if (source == null) {
            handleError(errorMessage + " (source is null)");
        }
        if (alg == null) {
            handleError(errorMessage + " (alg is null)");
        }
        try {
            if (!validateHash(source, alg, hash)) {
                handleError(errorMessage);
            }
        } catch (Exception ex) {
            /* any exception has to be caught in case of logging mode */
            handleError(ex.getMessage(), ex);
        }
    }

    private boolean validateHash(String value, String alg, String hash) {
        final byte[] digested = digestBasedOnAlg(alg, value);
        final byte[] hashBytesLeftHalf = Arrays.copyOf(digested, digested.length / 2);
        String result = EncodingUtils.encodeAsBase64UrlSafe(hashBytesLeftHalf);

        return Objects.equals(hash, result);
    }

    private boolean validateSignature(String keyId, String algorithm, String[] parts)
            throws InvalidKeyException, SignatureException, NoSuchAlgorithmException {
        String body = parts[0] + "." + parts[1];

        PublicKey publicKey = publicKeys.get(keyId);
        if (publicKey == null) {
            throw new IllegalArgumentException("Did not find key with id " + keyId);
        }

        Signature signature = signatureBasedOnAlg(algorithm);
        signature.initVerify(publicKey);
        signature.update(body.getBytes());
        byte[] rawSignature = EncodingUtils.decodeBase64String(parts[2]);
        return signature.verify(rawSignature);
    }

    private Signature signatureBasedOnAlg(String alg) throws NoSuchAlgorithmException {
        switch (alg) {
            case "PS512":
                return Signature.getInstance("SHA512withRSA/PSS");
            case "RS512":
                return Signature.getInstance("SHA512withRSA");
            case "PS384":
                return Signature.getInstance("SHA384withRSA/PSS");
            case "RS384":
                return Signature.getInstance("SHA384withRSA");
            case "PS256":
                return Signature.getInstance("SHA256withRSA/PSS");
            case "RS256":
                return Signature.getInstance("SHA256withRSA");
            default:
                // Consider adding support for other algorithms if used by any bank
                throw new UnsupportedOperationException("Not supported signing algorithm: " + alg);
        }
    }

    private byte[] digestBasedOnAlg(String alg, String value) {
        switch (alg) {
            case "ES512":
            case "HS512":
            case "PS512":
            case "RS512":
                return DigestUtils.sha512(value);
            case "ES384":
            case "HS384":
            case "PS384":
            case "RS384":
                return DigestUtils.sha384(value);
            case "ES256":
            case "HS256":
            case "PS256":
            case "RS256":
                return DigestUtils.sha256(value);
            default:
                throw new UnsupportedOperationException("Not supported signing algorithm: " + alg);
        }
    }

    private void handleError(String message) {
        if (mode == ValidatorMode.TERMINATING) {
            throw new IdTokenValidationException(message);
        } else {
            log.warn(message);
        }
    }

    private void handleError(String message, Throwable cause) {
        if (cause instanceof IdTokenValidationException) {
            if (mode == ValidatorMode.TERMINATING) {
                throw (IdTokenValidationException) cause;
            }
        } else {
            if (mode == ValidatorMode.TERMINATING) {
                throw new IdTokenValidationException(message, cause);
            } else {
                log.warn("ID Token validation failed: " + message, cause);
            }
        }
    }

    public enum ValidatorMode {
        TERMINATING,
        LOGGING
    }
}
