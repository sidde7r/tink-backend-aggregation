package se.tink.backend.aggregation.configuration.signaturekeypair;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.cryptography.RSAUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class SignatureKeyPair {
    @JsonIgnore private static final Logger log = LoggerFactory.getLogger(SignatureKeyPair.class);

    @JsonProperty private String privateKeyPath;
    @JsonProperty private String publicKeyPath;

    @JsonIgnore private RSAPrivateKey privateKey;
    @JsonIgnore private RSAPublicKey publicKey;
    @JsonIgnore private String keyId;

    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

    public String getPublicKeyPath() {
        return publicKeyPath;
    }

    public void setPublicKeyPath(String publicKeyPath) {
        this.publicKeyPath = publicKeyPath;
    }

    @JsonIgnore
    public RSAPrivateKey getPrivateKey() {
        if (Strings.isNullOrEmpty(privateKeyPath)) {
            log.info(
                    "The path to get the private key was null / empty. Make sure to add a path to the config.");
            return null;
        }

        if (privateKey == null) {
            this.privateKey = RSAUtils.getPrivateKey(privateKeyPath);
        }

        return privateKey;
    }

    @JsonIgnore
    public RSAPublicKey getPublicKey() {
        if (Strings.isNullOrEmpty(publicKeyPath)) {
            log.info(
                    "The path to get the public key was null / empty. Make sure to add a path to the config.");
            return null;
        }

        if (publicKey == null) {
            this.publicKey = RSAUtils.getPublicKey(publicKeyPath);
        }

        return publicKey;
    }

    @JsonIgnore
    public String getKeyId() {
        final RSAPublicKey pKey = getPublicKey();
        if (pKey == null) {
            log.error("Could not get the public key to calculate the keyId.");
            return null;
        }

        if (this.keyId == null) {
            // Changing algorithm will change all keyIds
            byte[] digest = sha("SHA-256", pKey.getEncoded());
            this.keyId = Hex.encodeHexString(digest).toLowerCase();
        }

        return keyId;
    }

    @JsonIgnore
    private static byte[] sha(String algorithm, final byte[]... datas) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            for (byte[] data : datas) {
                md.update(data);
            }
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
