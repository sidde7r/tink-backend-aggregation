package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.authenticator;

import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.CreditAgricoleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.CreditAgricoleConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.CreditAgricoleConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.configuration.CreditAgricoleConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class CreditAgricoleAuthenticator implements Authenticator {

    private final CreditAgricoleApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final CreditAgricoleConfiguration configuration;

    public CreditAgricoleAuthenticator(
            CreditAgricoleApiClient apiClient,
            PersistentStorage persistentStorage,
            CreditAgricoleConfiguration configuration) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
    }

    private CreditAgricoleConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, configuration.getToken());
    }
}
