package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vavr.control.Option;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@JsonObject
public class MetroAuthenticationData {
    private static final Supplier<IllegalArgumentException> KEY_DOES_NOT_EXIST =
            () -> new IllegalArgumentException("Key doesn't exist");
    private final Map<KeyType, KeyPairEntity> keys = new EnumMap<>(KeyType.class);
    private boolean isAlreadyRegistered;
    private String deviceId;
    private String internalDeviceId;
    private String userId;
    private String password;
    private String securedNumber;

    public MetroAuthenticationData setAlreadyRegistered(boolean alreadyRegistered) {
        isAlreadyRegistered = alreadyRegistered;
        return this;
    }

    public MetroAuthenticationData setRsaKeyPair(KeyPair rsaKeyPair) {
        insertKey(rsaKeyPair, KeyType.RSA_KEY_PAIR);
        return this;
    }

    public MetroAuthenticationData setChallengeSignESKeyPair(KeyPair challengeSignECKeyPair) {
        insertKey(challengeSignECKeyPair, KeyType.CHALLENGE_EC_KEY_PAIR);
        return this;
    }

    public MetroAuthenticationData setSignatureESKeyPair(KeyPair signatureECKeyPair) {
        insertKey(signatureECKeyPair, KeyType.SIGNATURE_EC_KEY_PAIR);
        return this;
    }

    private void insertKey(KeyPair signatureECKeyPair, KeyType signatureEcKeyPair) {
        keys.putIfAbsent(
                signatureEcKeyPair,
                KeyPairEntity.builder()
                        .privateKey(
                                Base64.getEncoder()
                                        .encodeToString(
                                                signatureECKeyPair.getPrivate().getEncoded()))
                        .publicKey(
                                Base64.getEncoder()
                                        .encodeToString(
                                                signatureECKeyPair.getPublic().getEncoded()))
                        .build());
    }

    public MetroAuthenticationData setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public MetroAuthenticationData setInternalDeviceId(String deviceName) {
        this.internalDeviceId = deviceName;
        return this;
    }

    public MetroAuthenticationData setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public MetroAuthenticationData setPassword(String password) {
        this.password = password;
        return this;
    }

    public MetroAuthenticationData setSecuredNumber(String securedNumber) {
        this.securedNumber = securedNumber;
        return this;
    }

    @JsonIgnore
    public KeyPair getChallengeKeyPair() {
        return Option.of(keys.get(KeyType.CHALLENGE_EC_KEY_PAIR))
                .map(KeyType.CHALLENGE_EC_KEY_PAIR::convert)
                .getOrElseThrow(KEY_DOES_NOT_EXIST);
    }

    @JsonIgnore
    public KeyPair getSigningKeyPair() {
        return Option.of(keys.get(KeyType.SIGNATURE_EC_KEY_PAIR))
                .map(KeyType.SIGNATURE_EC_KEY_PAIR::convert)
                .getOrElseThrow(KEY_DOES_NOT_EXIST);
    }

    public String getRSAPublicKey() {
        return Option.of(keys.get(KeyType.RSA_KEY_PAIR))
                .map(KeyPairEntity::getPublicKey)
                .getOrElseThrow(KEY_DOES_NOT_EXIST);
    }

    public String getSigningPublicKey() {
        return Option.of(keys.get(KeyType.SIGNATURE_EC_KEY_PAIR))
                .map(KeyPairEntity::getPublicKey)
                .getOrElseThrow(KEY_DOES_NOT_EXIST);
    }

    @JsonObject
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class KeyPairEntity {
        private String privateKey;
        private String publicKey;
    }

    @JsonObject
    public enum KeyType {
        SIGNATURE_EC_KEY_PAIR(
                encodedKey -> getPublicKey(encodedKey, "EC"),
                encodedKey -> getPrivateKey(encodedKey, "EC")),
        CHALLENGE_EC_KEY_PAIR(
                encodedKey -> getPublicKey(encodedKey, "EC"),
                encodedKey -> getPrivateKey(encodedKey, "EC")),
        RSA_KEY_PAIR(
                encodedKey -> getPublicKey(encodedKey, "RSA"),
                encodedKey -> getPrivateKey(encodedKey, "RSA"));

        private final Function<String, PublicKey> publicKeyConverter;

        private final Function<String, PrivateKey> privateKeyConverter;

        KeyType(
                Function<String, PublicKey> publicKeyConverter,
                Function<String, PrivateKey> privateKeyConverter) {
            this.publicKeyConverter = publicKeyConverter;
            this.privateKeyConverter = privateKeyConverter;
        }

        @JsonIgnore
        public KeyPair convert(KeyPairEntity securityKey) {
            return new KeyPair(
                    this.publicKeyConverter.apply(securityKey.getPublicKey()),
                    this.privateKeyConverter.apply(securityKey.getPrivateKey()));
        }

        private static PrivateKey getPrivateKey(String base64, String rsa) {
            try {
                KeyFactory keyFactory = KeyFactory.getInstance(rsa);
                return keyFactory.generatePrivate(
                        new PKCS8EncodedKeySpec(EncodingUtils.decodeBase64String(base64)));
            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                throw new IllegalArgumentException("Stored keys can't be recreated", e);
            }
        }

        private static PublicKey getPublicKey(String base64, String ec) {
            try {
                KeyFactory keyFactory = KeyFactory.getInstance(ec);
                return keyFactory.generatePublic(
                        new X509EncodedKeySpec(EncodingUtils.decodeBase64String(base64)));
            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                throw new IllegalArgumentException("Stored keys can't be recreated", e);
            }
        }
    }
}
