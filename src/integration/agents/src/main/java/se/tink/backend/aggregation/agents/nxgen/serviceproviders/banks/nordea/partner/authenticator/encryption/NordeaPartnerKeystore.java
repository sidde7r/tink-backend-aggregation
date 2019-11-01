package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.authenticator.encryption;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.configuration.NordeaPartnerConfiguration;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class NordeaPartnerKeystore {

    private RSAPublicKey nordeaSigningPublicKey;
    private RSAPublicKey nordeaEncryptionPublicKey;
    private RSAPrivateKey tinkSigningKey;
    private RSAPrivateKey tinkEncryptionKey;

    public NordeaPartnerKeystore(NordeaPartnerConfiguration configuration) {
        this.loadFrom(configuration);
    }

    private void loadFrom(NordeaPartnerConfiguration configuration) {
        Preconditions.checkNotNull(configuration, "Agent configuration is missing!");
        nordeaSigningPublicKey = getPubKeyFromBase64(configuration.getNordeaSigningPublicKey());
        nordeaEncryptionPublicKey =
                getPubKeyFromBase64(configuration.getNordeaEncryptionPublicKey());

        tinkSigningKey =
                getPrivateKeyFromBase64(
                        configuration.getTinkSingingPrivateKey(),
                        configuration.getTinkSingingKeyPassword());

        tinkEncryptionKey =
                getPrivateKeyFromBase64(
                        configuration.getTinkEncryptionPrivateKey(),
                        configuration.getTinkEncryptionKeyPassword());
    }

    private RSAPrivateKey getPrivateKeyFromBase64(String privateKeyString, String passphrase) {
        try {
            PBEKeySpec pbeKeySpec = new PBEKeySpec(passphrase.toCharArray());
            EncryptedPrivateKeyInfo encryptedPrivKeyInfo =
                    new EncryptedPrivateKeyInfo(EncodingUtils.decodeBase64String(privateKeyString));
            SecretKeyFactory secretKeyFactory =
                    SecretKeyFactory.getInstance(encryptedPrivKeyInfo.getAlgName());
            Key secret = secretKeyFactory.generateSecret(pbeKeySpec);
            PKCS8EncodedKeySpec pkcs8PrivKeySpec = encryptedPrivKeyInfo.getKeySpec(secret);
            return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(pkcs8PrivKeySpec);
        } catch (NoSuchAlgorithmException
                | InvalidKeySpecException
                | InvalidKeyException
                | IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private RSAPublicKey getPubKeyFromBase64(String publicKeyString) {
        return RSA.getPubKeyFromBytes(EncodingUtils.decodeBase64String(publicKeyString));
    }

    public RSAPublicKey getNordeaSigningPublicKey() {
        return nordeaSigningPublicKey;
    }

    public RSAPublicKey getNordeaEncryptionPublicKey() {
        return nordeaEncryptionPublicKey;
    }

    public RSAPrivateKey getTinkSigningKey() {
        return tinkSigningKey;
    }

    public RSAPrivateKey getTinkEncryptionKey() {
        return tinkEncryptionKey;
    }
}
