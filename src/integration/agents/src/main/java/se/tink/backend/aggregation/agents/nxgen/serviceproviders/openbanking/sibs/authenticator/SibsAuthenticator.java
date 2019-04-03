package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.configuration.SibsConfiguration;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SibsAuthenticator {

    private final SibsBaseApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final SibsConfiguration configuration;

    public SibsAuthenticator(
            SibsBaseApiClient apiClient,
            PersistentStorage persistentStorage,
            SibsConfiguration configuration) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
    }

    private SibsConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    public URL buildAuthorizeUrl(String state) {
        return apiClient.buildAuthorizeUrl(state);
    }

    public boolean isAuthorized() {
        ConsentStatusResponse consentStatusResponse = apiClient.getConsentStatus();

        return consentStatusResponse
                .getTransactionStatus()
                .equalsIgnoreCase(SibsConstants.ConsentStatuses.ACCEPTED_TECHNICAL_VALIDATION);
    }
}
