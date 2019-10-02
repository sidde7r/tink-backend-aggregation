package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.authenticator.encryption;

import com.google.common.base.Preconditions;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.configuration.NordeaPartnerConfiguration;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class NordeaPartnerKeystore {

    private RSAPublicKey nordeaSigningPublicKey;
    private RSAPublicKey nordeaEncryptionPublicKey;
    private KeyPair tinkSigningKeyPair;
    private KeyPair tinkEncryptionKeyPair;

    public NordeaPartnerKeystore(NordeaPartnerConfiguration configuration) {
        this.loadFrom(configuration);
    }

    private void loadFrom(NordeaPartnerConfiguration configuration) {
        Preconditions.checkNotNull(configuration, "Agent configuration is missing!");
        nordeaSigningPublicKey = getPubKeyFromBase64(configuration.getNordeaSigningPublicKey());
        nordeaEncryptionPublicKey =
                getPubKeyFromBase64(configuration.getNordeaEncryptionPublicKey());

        tinkSigningKeyPair =
                getKeypair(
                        configuration.getTinkSingingPrivateKey(),
                        configuration.getTinkSingingPublicKey());

        tinkEncryptionKeyPair =
                getKeypair(
                        configuration.getTinkEncryptionPrivateKey(),
                        configuration.getTinkEncryptionPublicKey());
    }

    private KeyPair getKeypair(String privateKeyString, String publicKeyString) {
        PublicKey publicKey = getPubKeyFromBase64(publicKeyString);
        PrivateKey privateKey = getPrivateKeyFromBase64(privateKeyString);
        return new KeyPair(publicKey, privateKey);
    }

    private RSAPrivateKey getPrivateKeyFromBase64(String privateKeyString) {
        return RSA.getPrivateKeyFromBytes(EncodingUtils.decodeBase64String(privateKeyString));
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

    public KeyPair getTinkSigningKeyPair() {
        return tinkSigningKeyPair;
    }

    public KeyPair getTinkEncryptionKeyPair() {
        return tinkEncryptionKeyPair;
    }
}
