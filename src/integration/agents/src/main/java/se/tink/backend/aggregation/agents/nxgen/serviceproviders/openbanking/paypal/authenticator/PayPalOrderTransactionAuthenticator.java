package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.authenticator;

import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.configuration.PayPalConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class PayPalOrderTransactionAuthenticator implements Authenticator {

    private final PayPalApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final PayPalConfiguration configuration;

    public PayPalOrderTransactionAuthenticator(
            PayPalApiClient apiClient,
            PersistentStorage persistentStorage,
            PayPalConfiguration configuration) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
    }

    private PayPalConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        OAuth2Token accessToken = apiClient.getClientCredentialsToken();
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, accessToken);
    }
}
