package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.authenticator;

import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.TriodosApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.BerlinGroupAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class TriodosAuthenticator extends BerlinGroupAuthenticator {

    private final TriodosApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public TriodosAuthenticator(
            final TriodosApiClient apiClient, PersistentStorage persistentStorage) {
        super(apiClient);
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(final String code) throws BankServiceException {
        final OAuth2Token token = apiClient.getToken(code);
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, token);

        return token;
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) throws BankServiceException {
        final OAuth2Token token = apiClient.refreshToken(refreshToken);
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, token);

        return token;
    }
}
