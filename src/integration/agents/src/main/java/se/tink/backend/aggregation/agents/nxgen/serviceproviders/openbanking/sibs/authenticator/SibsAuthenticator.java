package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsUserState;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AutomaticAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.ThirdPartyAppAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;

public class SibsAuthenticator extends StatelessProgressiveAuthenticator {

    private static final int CONSENTS_LIFETIME_IN_DAYS = 90;
    private final SibsUserState userState;
    private final List<AuthenticationStep> authSteps = new LinkedList<>();
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
    public List<AuthenticationStep> authenticationSteps() {
        if (authSteps.isEmpty()) {
            SibsThirdPartyAppRequestParamsProvider sibsThirdPartyAppRequestParamsProvider =
                    new SibsThirdPartyAppRequestParamsProvider(
                            consentManager, this, strongAuthenticationState);
            authSteps.add(
                    new AutomaticAuthenticationStep(
                            () -> processAutoAuthentication(), "autoAuthenticationStep"));
            authSteps.add(
                    new ThirdPartyAppAuthenticationStep(
                            SibsThirdPartyAppRequestParamsProvider.STEP_ID,
                            sibsThirdPartyAppRequestParamsProvider,
                            sibsThirdPartyAppRequestParamsProvider::processThirdPartyCallback));
        }
        return authSteps;
    }

    private AuthenticationStepResponse processAutoAuthentication() {
        if (isAutoAuthenticationPossible()) {
            return AuthenticationStepResponse.authenticationSucceeded();
        } else {
            return AuthenticationStepResponse.executeNextStep();
        }
    }

    private boolean isAutoAuthenticationPossible() {
        try {
            return !userState.isManualAuthenticationInProgress()
                    && consentManager.getStatus().isAcceptedStatus();
        } catch (SessionException | BankServiceException e) {
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
