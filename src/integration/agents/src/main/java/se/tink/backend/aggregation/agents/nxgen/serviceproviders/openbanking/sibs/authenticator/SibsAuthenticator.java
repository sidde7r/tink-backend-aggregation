package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.http.URL;

public class SibsAuthenticator {

    private final SibsBaseApiClient apiClient;

    public SibsAuthenticator(SibsBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public URL buildAuthorizeUrl(String state) {
        return apiClient.buildAuthorizeUrl(state);
    }

    public ConsentStatus getConsentStatus() {
        String consentStatusString = "unknown state";
        try {
            consentStatusString = apiClient.getConsentStatus().getTransactionStatus();
            return ConsentStatus.valueOf(consentStatusString);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    SibsConstants.ErrorMessages.UNKNOWN_TRANSACTION_STATE
                            + "="
                            + consentStatusString,
                    e);
        }
    }

    public ConsentResponse initializeDecoupledConsent(
            String state, String psuIdType, String psuId) {
        return apiClient.createDecoupledAuthConsent(state, psuIdType, psuId);
    }

    public void autoAuthenticate() throws SessionException {
        ConsentStatus consentStatus = getConsentStatus();

        if (!consentStatus.isAcceptedStatus()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
