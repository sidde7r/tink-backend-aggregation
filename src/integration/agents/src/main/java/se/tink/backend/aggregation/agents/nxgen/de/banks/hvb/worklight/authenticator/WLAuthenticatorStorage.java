package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator;

import java.security.KeyPair;
import java.util.Optional;

public interface WLAuthenticatorStorage {

    void setWlInstanceId(String wlInstanceId);

    /** @return A serialized public/private RSA key pair, if it exists in this storage */
    Optional<KeyPair> getKeyPair();

    void setKeyPair(KeyPair keyPair);
}
