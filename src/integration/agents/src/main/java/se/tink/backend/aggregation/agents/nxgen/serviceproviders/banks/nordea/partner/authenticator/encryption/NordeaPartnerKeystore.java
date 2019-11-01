package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.authenticator.encryption;

import com.google.common.base.Preconditions;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
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

        tinkSigningKey = getPrivateKeyFromBase64(configuration.getTinkSingingPrivateKey());

        tinkEncryptionKey = getPrivateKeyFromBase64(configuration.getTinkEncryptionPrivateKey());
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

    public RSAPrivateKey getTinkSigningKey() {
        return tinkSigningKey;
    }

    public RSAPrivateKey getTinkEncryptionKey() {
        return tinkEncryptionKey;
    }
}
