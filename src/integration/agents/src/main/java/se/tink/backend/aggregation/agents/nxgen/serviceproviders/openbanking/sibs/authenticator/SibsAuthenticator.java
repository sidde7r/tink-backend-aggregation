package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsUserState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.MessageCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SibsAuthenticator {

    private static final int NINETY_DAYS = 90;
    private final SibsBaseApiClient apiClient;
    private final Credentials credentials;
    private final SibsUserState userState;

    private enum AuthenticationState {
        MANUAL_ON_GOING,
        MANUAL_SUCCEEDED,
        AUTO
    }

    public SibsAuthenticator(
            SibsBaseApiClient apiClient, SibsUserState userState, Credentials credentials) {
        this.apiClient = apiClient;
        this.credentials = credentials;
        this.userState = userState;
    }

    public URL buildAuthorizeUrl(String state) {
        return apiClient.buildAuthorizeUrl(state);
    }

    public ConsentStatus getConsentStatus() throws SessionException {
        return mapToConsentStatus(apiClient.getConsentStatus());
    }

    private AuthenticationState getCurrentAuthenticationState() throws SessionException {
        ConsentStatus consentStatus = getConsentStatus();
        final boolean manualAuthenticationInProgress = userState.isManualAuthenticationInProgress();
        if (manualAuthenticationInProgress) {
            if (consentStatus.isAcceptedStatus()) {
                return AuthenticationState.MANUAL_SUCCEEDED;
            } else {
                return AuthenticationState.MANUAL_ON_GOING;
            }
        }
        return AuthenticationState.AUTO;
    }

    private ConsentStatus mapToConsentStatus(final ConsentStatusResponse response) {
        String consentStatusString = "unknown state";
        try {
            consentStatusString = response.getTransactionStatus();
            return ConsentStatus.valueOf(consentStatusString);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    SibsConstants.ErrorMessages.UNKNOWN_TRANSACTION_STATE
                            + "="
                            + consentStatusString,
                    e);
        }
    }

    public void autoAuthenticate() throws SessionException {
        try {
            AuthenticationState authenticationState = getCurrentAuthenticationState();
            if (authenticationState != AuthenticationState.AUTO) {
                if (authenticationState == AuthenticationState.MANUAL_SUCCEEDED) {
                    userState.finishManualAuthentication();
                }
                throw SessionError.SESSION_EXPIRED.exception();
            }
        } catch (HttpResponseException e) {
            handleInvalidConsents(e);
        }
    }

    public void setSessionExpiryDateIfAccepted(ConsentStatus consentStatus) {
        if (consentStatus.isAcceptedStatus()) {
            Date sessionExpiryDate =
                    Date.from(
                            LocalDateTime.now()
                                    .plusDays(NINETY_DAYS)
                                    .atZone(ZoneId.systemDefault())
                                    .toInstant());

            credentials.setSessionExpiryDate(sessionExpiryDate);
        }
    }

    private void handleInvalidConsents(HttpResponseException rethrowIfNotConsentProblems)
            throws SessionException {
        final String message = rethrowIfNotConsentProblems.getResponse().getBody(String.class);
        if (isConsentsProblem(message)) {
            userState.removeConsent();
            throw SessionError.SESSION_EXPIRED.exception(rethrowIfNotConsentProblems);
        }
        throw rethrowIfNotConsentProblems;
    }

    private boolean isConsentsProblem(String message) {
        return message.contains(MessageCodes.CONSENT_INVALID.name())
                || message.contains(MessageCodes.CONSENT_EXPIRED.name())
                || message.contains(MessageCodes.CONSENT_UNKNOWN.name());
    }
}
