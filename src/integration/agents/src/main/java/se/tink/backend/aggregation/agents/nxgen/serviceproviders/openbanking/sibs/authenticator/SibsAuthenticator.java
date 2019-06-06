package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator;

import java.util.Arrays;
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
        return Arrays.stream(ConsentStatus.values())
                .filter(
                        v ->
                                v.toString()
                                        .equalsIgnoreCase(
                                                apiClient
                                                        .getConsentStatus()
                                                        .getTransactionStatus()))
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        SibsConstants.ErrorMessages.UNKNOWN_TRANSACTION_STATE
                                                + ": "
                                                + apiClient
                                                        .getConsentStatus()
                                                        .getTransactionStatus()));
    }

    public ConsentResponse initializeConsent(String state, String psuIdType, String psuId) {
        return apiClient.createDecoupledAuthConsent(state, psuIdType, psuId);
    }
}
