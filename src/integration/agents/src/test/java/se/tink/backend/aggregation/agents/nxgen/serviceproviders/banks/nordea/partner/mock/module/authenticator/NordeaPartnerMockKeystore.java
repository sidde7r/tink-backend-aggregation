package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.mock.module.authenticator;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.authenticator.encryption.NordeaPartnerKeystore;

public class NordeaPartnerMockKeystore implements NordeaPartnerKeystore {
    final RSAPublicKey encryptionKey;
    final RSAPrivateKey signingKey;

    public NordeaPartnerMockKeystore() {
        final KeyPairGenerator keyGen;
        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not initialize key generator for test", e);
        }
        final KeyPair encryptionKeyPair = keyGen.generateKeyPair();
        encryptionKey = (RSAPublicKey) encryptionKeyPair.getPublic();
        final KeyPair signingKeyPair = keyGen.generateKeyPair();
        signingKey = (RSAPrivateKey) signingKeyPair.getPrivate();
    }

    @Override
    public RSAPublicKey getNordeaEncryptionPublicKey() {
        return encryptionKey;
    }

    @Override
    public RSAPrivateKey getTinkSigningKey() {
        return signingKey;
    }
}
