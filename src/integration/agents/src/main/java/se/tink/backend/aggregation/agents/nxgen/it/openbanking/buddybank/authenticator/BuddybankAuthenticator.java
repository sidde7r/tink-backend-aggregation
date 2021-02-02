package se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.authenticator;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.BuddybankApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.authenticator.rpc.BuddybankCreateConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentStatusResponse;

@RequiredArgsConstructor
public class BuddybankAuthenticator {

    private final BuddybankApiClient apiClient;

    public BuddybankCreateConsentResponse createConsentRequest(String state) {
        return apiClient.createBuddybankConsent(state);
    }

    public ConsentStatusResponse getConsentStatus() throws SessionException {
        return apiClient.getConsentStatus();
    }
}
