package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.authenticator.encryption;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public interface NordeaPartnerKeystore {
    RSAPublicKey getNordeaEncryptionPublicKey();

    RSAPrivateKey getTinkSigningKey();
}
