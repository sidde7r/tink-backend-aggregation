package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.TransactionStatus;
import se.tink.backend.aggregation.nxgen.http.URL;

public class SibsAuthenticator {

    private final SibsBaseApiClient apiClient;

    public SibsAuthenticator(SibsBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public URL buildAuthorizeUrl(String state) {
        return apiClient.buildAuthorizeUrl(state);
    }

    public TransactionStatus getConsentStatus() {

        try {
            return TransactionStatus.valueOf(apiClient.getConsentStatus().getTransactionStatus());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(SibsConstants.ErrorMessages.UNKNOWN_TRANSACTION_STATE);
        }
    }
}
