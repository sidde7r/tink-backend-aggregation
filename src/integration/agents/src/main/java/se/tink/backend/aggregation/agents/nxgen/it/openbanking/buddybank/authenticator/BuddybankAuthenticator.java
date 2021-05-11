package se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.authenticator;

import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.BuddybankApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.authenticator.rpc.BuddybankConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.UnicreditAuthenticator;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class BuddybankAuthenticator extends UnicreditAuthenticator {

    private final BuddybankApiClient buddybankApiClient;

    public BuddybankAuthenticator(
            UnicreditBaseApiClient apiClient,
            UnicreditStorage unicreditStorage,
            Credentials credentials) {
        super(unicreditStorage, apiClient, credentials);
        buddybankApiClient = new BuddybankApiClient(apiClient);
    }

    public BuddybankConsentResponse createConsentRequest(String state) {
        return (BuddybankConsentResponse) buddybankApiClient.createConsent(state);
    }

    public ConsentDetailsResponse getConsentDetails() throws SessionException {
        return buddybankApiClient.getConsentDetails();
    }

    protected Optional<String> getConsentId() {
        return super.getConsentId();
    }

    protected void clearConsent() {
        super.clearConsent();
    }

    protected Optional<ConsentDetailsResponse> getConsentDetailsWithValidStatus() {
        return super.getConsentDetailsWithValidStatus();
    }

    protected void setCredentialsSessionExpiryDate(ConsentDetailsResponse consentDetailsResponse) {
        super.setCredentialsSessionExpiryDate(consentDetailsResponse);
    }

    protected URL buildAuthorizeUrl(String state) {
        return super.buildAuthorizeUrl(state);
    }
}
