package se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.authenticator.rpc.BuddybankConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.UnicreditAuthenticator;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;

public class BuddybankAuthenticator extends UnicreditAuthenticator {

    public BuddybankAuthenticator(
            UnicreditBaseApiClient apiClient,
            UnicreditStorage unicreditStorage,
            Credentials credentials) {
        super(apiClient, unicreditStorage, credentials);
    }

    public BuddybankConsentResponse createConsentRequest(String state) {
        return (BuddybankConsentResponse) apiClient.createConsent(state);
    }

    public ConsentDetailsResponse getConsentDetails() throws SessionException {
        return apiClient.getConsentDetails();
    }
}
