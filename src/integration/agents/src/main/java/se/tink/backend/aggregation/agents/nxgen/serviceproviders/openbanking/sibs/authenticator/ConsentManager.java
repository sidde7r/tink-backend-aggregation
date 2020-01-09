package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsUserState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.MessageCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class ConsentManager {

    private SibsBaseApiClient apiClient;
    private SibsUserState userState;
    private StrongAuthenticationState strongAuthenticationState;

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
}
