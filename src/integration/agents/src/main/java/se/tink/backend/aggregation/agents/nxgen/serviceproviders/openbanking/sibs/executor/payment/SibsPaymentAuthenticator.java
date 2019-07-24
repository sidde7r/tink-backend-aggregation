package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SibsPaymentAuthenticator {

    private final SibsBaseApiClient apiClient;

    public SibsPaymentAuthenticator(SibsBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public URL buildAuthorizeUrlForPayment(SessionStorage sessionStorage) {
        return apiClient.buildAuthorizeUrlForPayment(sessionStorage);
    }

    public ConsentStatus getConsentStatus() {
        try {
            return ConsentStatus.valueOf(apiClient.getConsentStatus().getTransactionStatus());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(SibsConstants.ErrorMessages.UNKNOWN_TRANSACTION_STATE);
        }
    }

    public ConsentResponse initializeConsent(String state, String psuIdType, String psuId) {
        return apiClient.createDecoupledAuthConsent(state, psuIdType, psuId);
    }
}
