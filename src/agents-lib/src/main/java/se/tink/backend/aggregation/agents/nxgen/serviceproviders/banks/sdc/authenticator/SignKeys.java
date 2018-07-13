package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator;

import java.security.KeyPair;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcPersistentStorage;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;

public class SignKeys {
    private RSAPrivateKey privateKey;
    private final SdcPersistentStorage persistentStorage;

    public SignKeys(SdcPersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;

        if (persistentStorage.hasSignerKeys()) {
            this.privateKey = RSA.getPrivateKeyFromBytes(persistentStorage.getPrivateKey());
        } else {
            generateKeys();
        }
    }
    private void generateKeys() {
        KeyPair keyPair = RSA.generateKeyPair();

        this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        this.persistentStorage.putPrivateKey(this.privateKey.getEncoded());
        this.persistentStorage.putPublicKey(publicKey.getEncoded());
    }

    public RSAPrivateKey getPrivateKey() {
        return this.privateKey;
    }

    public String getPublicKey() {
        return this.persistentStorage.getPublicKey();
    }
}
