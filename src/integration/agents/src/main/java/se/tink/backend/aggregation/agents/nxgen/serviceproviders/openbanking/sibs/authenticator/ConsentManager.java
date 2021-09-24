package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsUserState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.MessageCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class ConsentManager {

    private final SibsBaseApiClient apiClient;
    private final SibsUserState userState;
    private final StrongAuthenticationState strongAuthenticationState;

    ConsentStatus getStatus() {
        ConsentStatus consentStatus;
        try {
            consentStatus = apiClient.getConsentStatus();
        } catch (HttpResponseException e) {
            final String message = e.getResponse().getBody(String.class);
            if (MessageCodes.isConsentProblem(message)) {
                userState.resetAuthenticationState();
                throw SessionError.SESSION_EXPIRED.exception(e);
            }
            throw e;
        }
        return consentStatus;
    }

    URL create() {
        ConsentResponse response = apiClient.createConsent(strongAuthenticationState.getState());
        userState.startManualAuthentication(response.getConsentId());
        return new URL(response.getLinks().getRedirect());
    }
}
