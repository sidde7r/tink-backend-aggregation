package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.http.URL;

public class SibsAuthenticator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SibsAuthenticator.class.getName());

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
}
