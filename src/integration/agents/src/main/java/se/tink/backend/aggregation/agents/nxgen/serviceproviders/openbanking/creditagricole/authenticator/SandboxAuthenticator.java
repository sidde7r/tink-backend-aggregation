package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration.CreditAgricoleBaseConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SandboxAuthenticator implements Authenticator {

    private final PersistentStorage persistentStorage;
    private final CreditAgricoleBaseConfiguration configuration;

    public SandboxAuthenticator(
            PersistentStorage persistentStorage, CreditAgricoleBaseConfiguration configuration) {
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
    }

    @Override
    public void authenticate(Credentials credentials) {
        persistentStorage.put(
                CreditAgricoleBaseConstants.StorageKeys.OAUTH_TOKEN, configuration.getToken());
    }
}
