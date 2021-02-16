package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsUserState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.MessageCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class ConsentManager {

    private final SibsBaseApiClient apiClient;
    private final SibsUserState userState;
    private final StrongAuthenticationState strongAuthenticationState;

    ConsentManager(
            SibsBaseApiClient apiClient,
            SibsUserState userState,
            StrongAuthenticationState strongAuthenticationState) {
        this.apiClient = apiClient;
        this.userState = userState;
        this.strongAuthenticationState = strongAuthenticationState;
    }

    ConsentStatus getStatus() throws SessionException {
        try {
            return apiClient.getConsentStatus();
        } catch (HttpResponseException e) {
            if (isRateLimitExceededResponse(e.getResponse())) {
                throw BankServiceError.ACCESS_EXCEEDED.exception();
            }
            final String message = e.getResponse().getBody(String.class);
            if (MessageCodes.isConsentProblem(message)) {
                userState.resetAuthenticationState();
                throw SessionError.SESSION_EXPIRED.exception(e);
            }
            throw e;
        }
    }

    URL create() {
        ConsentResponse response = apiClient.createConsent(strongAuthenticationState.getState());
        userState.startManualAuthentication(response.getConsentId());
        return new URL(response.getLinks().getRedirect());
    }

    private boolean isRateLimitExceededResponse(HttpResponse response) {
        return response.getStatus() == 429;
    }
}
