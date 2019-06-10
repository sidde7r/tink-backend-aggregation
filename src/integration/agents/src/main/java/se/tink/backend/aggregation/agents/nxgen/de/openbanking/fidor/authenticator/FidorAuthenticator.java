package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator;

import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.FidorApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.FidorConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.FidorConstants.FieldKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.FidorConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator.rpc.AuthorizationConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.configuration.FidorConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class FidorAuthenticator implements Authenticator {

    private final FidorApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final FidorConfiguration configuration;

    public FidorAuthenticator(
            FidorApiClient apiClient,
            PersistentStorage persistentStorage,
            FidorConfiguration configuration) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
    }

    private FidorConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    public void authenticate(Credentials credentials) {

        String username = credentials.getField(FieldKeys.USERNAME);
        String password = credentials.getField(FieldKeys.PASSWORD);

        // getting token
        OAuth2Token token = apiClient.OAut2_Password(username, password).toTinkToken();
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, token);

        String iban = credentials.getField(FieldKeys.IBAN);
        String bban = credentials.getField(FieldKeys.BBAN);

        // getting consent
        ConsentResponse consentResponse = apiClient.getConsent(iban, bban);
        persistentStorage.put(StorageKeys.CONSENT_ID, consentResponse.getConsentId());

        // authorization of the consent
        AuthorizationConsentResponse authroizationResponse =
                apiClient.authorizeConsent("OTPOTP", consentResponse.getAuthorizationLink());
    }
}
