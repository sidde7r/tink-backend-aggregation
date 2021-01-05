package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication;

import java.util.LinkedList;
import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.BPostBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request.dto.LoginResponseDTO;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.entity.BPostBankAuthContext;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AutomaticAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.PinCodeGeneratorAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BPostBankAuthenticator extends StatelessProgressiveAuthenticator {

    private static final String LOGIN_PIN_INIT_STEP_ID = "loginStep";

    private final List<AuthenticationStep> authSteps = new LinkedList<>();
    private final BPostBankApiClient apiClient;
    private final BPostBankAuthContext authContext;
    private final SupplementalInformationFormer supplementalInformationFormer;

    public BPostBankAuthenticator(
            final BPostBankApiClient apiClient,
            final BPostBankAuthContext authContext,
            final CredentialsRequest credentialsRequest) {
        this.apiClient = apiClient;
        this.authContext = authContext;
        this.supplementalInformationFormer =
                new SupplementalInformationFormer(credentialsRequest.getProvider());
    }

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        if (authSteps.isEmpty()) {
            authSteps.add(
                    new AutomaticAuthenticationStep(
                            this::initSessionCallbackHandler, "initSessionStep"));
            authSteps.add(
                    new BPostBankSigningAuthenticationStep(
                            supplementalInformationFormer, apiClient, authContext));
            authSteps.add(
                    new PinCodeGeneratorAuthenticationStep(
                            this::pinCodeGenerationCallbackHandler, 6));
            authSteps.add(new AutomaticAuthenticationStep(this::login, LOGIN_PIN_INIT_STEP_ID));
        }
        return authSteps;
    }

    private boolean isManualAuthentication() {
        return !authContext.isRegistrationCompleted();
    }

    private AuthenticationStepResponse initSessionCallbackHandler() throws AuthenticationException {
        authContext.setCsrfToken(apiClient.initSessionAndGetCSRFToken());
        return isManualAuthentication()
                ? AuthenticationStepResponse.executeNextStep()
                : AuthenticationStepResponse.executeStepWithId(LOGIN_PIN_INIT_STEP_ID);
    }

    private AuthenticationStepResponse pinCodeGenerationCallbackHandler(String pin)
            throws AuthenticationException {
        authContext.setPin(pin);
        authContext.completeRegistration(apiClient.registrationExecute(authContext));
        return AuthenticationStepResponse.executeNextStep();
    }

    private AuthenticationStepResponse login() throws AuthenticationException {
        LoginResponseDTO response = apiClient.loginPINInit(authContext);
        authContext.setSessionToken(response.getSessionToken());
        response = apiClient.loginPINAuth(authContext);
        if (response.isMobileAccessDeletedError()) {
            authContext.clearRegistrationData();
            return AuthenticationStepResponse.executeStepWithId(
                    findStepIdStartingManualAuthentication());
        }
        return AuthenticationStepResponse.executeNextStep();
    }

    private String findStepIdStartingManualAuthentication() {
        return authenticationSteps().stream()
                .filter(s -> s.getClass().equals(BPostBankSigningAuthenticationStep.class))
                .findAny()
                .get()
                .getIdentifier();
    }
}
