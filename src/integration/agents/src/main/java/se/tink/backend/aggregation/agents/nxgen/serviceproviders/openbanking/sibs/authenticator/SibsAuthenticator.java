package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsUserState;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SibsAuthenticator extends StatelessProgressiveAuthenticator {

    private static final int CONSENTS_LIFETIME_IN_DAYS = 90;
    private final SibsUserState userState;
    private final List<AuthenticationStep> manualAuthSteps = new LinkedList<>();
    private final StrongAuthenticationState strongAuthenticationState;
    private final ConsentManager consentManager;
    private final Credentials credentials;

    public SibsAuthenticator(
            SibsBaseApiClient apiClient,
            SibsUserState userState,
            Credentials credentials,
            StrongAuthenticationState strongAuthenticationState) {
        this.userState = userState;
        this.strongAuthenticationState = strongAuthenticationState;
        this.consentManager = new ConsentManager(apiClient, userState, strongAuthenticationState);
        this.credentials = credentials;
    }

    @Override
    public Iterable<? extends AuthenticationStep> authenticationSteps()
            throws AuthenticationException, AuthorizationException {
        if (isAutoAuthenticationPossible()) {
            return Collections.emptyList();
        }
        return getManualAuthenticationSteps();
    }

    private List<AuthenticationStep> getManualAuthenticationSteps() {
        if (manualAuthSteps.isEmpty()) {
            manualAuthSteps.add(
                    SibsThirdPartyAuthenticationStep.create(
                            consentManager, this, strongAuthenticationState));
        }
        return manualAuthSteps;
    }

    @Override
    public boolean isManualAuthentication(CredentialsRequest request) {
        return !isAutoAuthenticationPossible();
    }

    private boolean isAutoAuthenticationPossible() {
        try {
            return !userState.isManualAuthenticationInProgress()
                    && consentManager.getStatus().isAcceptedStatus();
        } catch (SessionException e) {
            return false;
        }
    }

    void handleManualAuthenticationSuccess() {
        Date sessionExpiryDate =
                Date.from(
                        LocalDateTime.now()
                                .plusDays(CONSENTS_LIFETIME_IN_DAYS)
                                .atZone(ZoneId.systemDefault())
                                .toInstant());

        credentials.setSessionExpiryDate(sessionExpiryDate);
        userState.finishManualAuthentication();
    }

    void handleManualAuthenticationFailure() {
        userState.resetAuthenticationState();
    }
}
