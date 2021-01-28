package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.authenticator.encryption;

import com.google.common.base.Preconditions;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.configuration.NordeaPartnerConfiguration;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class NordeaPartnerKeystoreImpl implements NordeaPartnerKeystore {

    private RSAPublicKey nordeaEncryptionPublicKey;
    private RSAPrivateKey tinkSigningKey;

    public NordeaPartnerKeystoreImpl(NordeaPartnerConfiguration configuration, String clusterId) {
        this.loadFrom(configuration, clusterId);
    }

    private void loadFrom(NordeaPartnerConfiguration configuration, String clusterId) {
        Preconditions.checkNotNull(configuration, "Agent configuration is missing!");
        nordeaEncryptionPublicKey =
                getPubKeyFromBase64(configuration.getNordeaEncryptionPublicKey());

        KeyStore keystore =
                loadKeyStore(
                        getKeystorePath(clusterId), configuration.getPartnerKeystorePassword());
        tinkSigningKey =
                getPrivateKeyFromKeystore(
                        keystore,
                        NordeaPartnerConstants.Keystore.SIGNING_KEY_ALIAS,
                        configuration.getPartnerKeystorePassword());
    }

    private String getKeystorePath(String clusterId) {
        return NordeaPartnerConstants.Keystore.KEYSTORE_PATH.replace("{clusterId}", clusterId);
    }

    private KeyStore loadKeyStore(String keyStorePath, String password) {
        try (FileInputStream is = new FileInputStream(keyStorePath)) {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(is, password.toCharArray());
            return ks;
        } catch (KeyStoreException
                | IOException
                | NoSuchAlgorithmException
                | CertificateException e) {
            throw new IllegalStateException(e);
        }
    }

    private RSAPrivateKey getPrivateKeyFromKeystore(
            KeyStore keyStore, String alias, String password) {
        try {
            Key key = keyStore.getKey(alias, password.toCharArray());
            if (key instanceof PrivateKey) {
                return (RSAPrivateKey) key;
            } else {
                throw new IllegalStateException("Unrecoverable Key: Not a private key " + alias);
            }
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new IllegalStateException(e);
        }
    }

    private RSAPublicKey getPubKeyFromBase64(String publicKeyString) {
        return RSA.getPubKeyFromBytes(EncodingUtils.decodeBase64String(publicKeyString));
    }

    @Override
    public RSAPublicKey getNordeaEncryptionPublicKey() {
        return nordeaEncryptionPublicKey;
    }

    @Override
    public RSAPrivateKey getTinkSigningKey() {
        return tinkSigningKey;
    }
}
