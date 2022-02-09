package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.assertj.core.util.Lists;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.SelectOption;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsUserState;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AutomaticAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.ThirdPartyAppAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public class SibsAuthenticator extends StatelessProgressiveAuthenticator {

    private static final int CONSENTS_LIFETIME_IN_DAYS = 90;
    private final SibsUserState userState;
    private final List<AuthenticationStep> authSteps = new LinkedList<>();
    private final StrongAuthenticationState strongAuthenticationState;
    private final ConsentManager consentManager;
    private final CredentialsRequest credentialsRequest;
    private final LocalDateTimeSource localDateTimeSource;

    public SibsAuthenticator(
            SibsBaseApiClient apiClient,
            SibsUserState userState,
            CredentialsRequest credentialsRequest,
            StrongAuthenticationState strongAuthenticationState,
            LocalDateTimeSource localDateTimeSource) {
        this.userState = userState;
        this.strongAuthenticationState = strongAuthenticationState;
        this.consentManager = new ConsentManager(apiClient, userState, strongAuthenticationState);
        this.credentialsRequest = credentialsRequest;
        this.localDateTimeSource = localDateTimeSource;
    }

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        if (authSteps.isEmpty()) {
            authSteps.add(
                    new AccountSegmentSpecificationAuthenticationStep(
                            userState, credentialsRequest, prepareAccountSegmentField()));
            SibsThirdPartyAppRequestParamsProvider sibsThirdPartyAppRequestParamsProvider =
                    new SibsThirdPartyAppRequestParamsProvider(
                            consentManager, this, strongAuthenticationState, localDateTimeSource);
            authSteps.add(
                    new AutomaticAuthenticationStep(
                            this::processAutoAuthentication, "autoAuthenticationStep"));
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
        } catch (SessionException e) {
            return false;
        }
    }

    private Field prepareAccountSegmentField() {
        return Field.builder()
                .name("accountSegment")
                .selectOptions(
                        Lists.newArrayList(
                                new SelectOption(new LocalizableKey("Personal").get(), "PERSONAL"),
                                new SelectOption(new LocalizableKey("Business").get(), "BUSINESS")))
                .description(
                        new LocalizableKey("Which account segment do you want to aggregate?").get())
                .sensitive(false)
                .build();
    }

    void handleManualAuthenticationSuccess() {
        Date sessionExpiryDate =
                Date.from(
                        LocalDateTime.now()
                                .plusDays(CONSENTS_LIFETIME_IN_DAYS)
                                .atZone(ZoneId.systemDefault())
                                .toInstant());

        credentialsRequest.getCredentials().setSessionExpiryDate(sessionExpiryDate);
        userState.finishManualAuthentication();
    }

    void handleManualAuthenticationFailure() {
        userState.resetAuthenticationState();
    }
}
