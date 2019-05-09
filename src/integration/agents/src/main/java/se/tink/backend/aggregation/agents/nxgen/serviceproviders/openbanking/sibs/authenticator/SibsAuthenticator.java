package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.nxgen.http.URL;

public class SibsAuthenticator {

    private final SibsBaseApiClient apiClient;

    public SibsAuthenticator(SibsBaseApiClient apiClient) {
        this.apiClient = apiClient;
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
