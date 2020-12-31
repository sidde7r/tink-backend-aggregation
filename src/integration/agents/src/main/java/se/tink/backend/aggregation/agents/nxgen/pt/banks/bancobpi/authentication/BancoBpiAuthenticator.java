package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.request.*;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.common.DefaultRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiAuthContext;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiEntityManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AutomaticAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.OtpStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.PinCodeGeneratorAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.UsernamePasswordAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.libraries.i18n.LocalizableKey;

public class BancoBpiAuthenticator extends StatelessProgressiveAuthenticator {

    private List<AuthenticationStep> autoAuthenticationSteps;
    private List<AuthenticationStep> manualAuthenticationSteps;
    private TinkHttpClient httpClient;
    private final SupplementalInformationFormer supplementalInformationFormer;
    private BancoBpiAuthContext authContext;
    private BancoBpiEntityManager entityManager;

    public BancoBpiAuthenticator(
            final TinkHttpClient httpClient,
            final SupplementalInformationFormer supplementalInformationFormer,
            BancoBpiEntityManager entityManager) {
        this.httpClient = httpClient;
        this.supplementalInformationFormer = supplementalInformationFormer;
        this.entityManager = entityManager;
        this.authContext = entityManager.getAuthContext();
        initManualAuthenticationSteps();
        initAutoAuthenticationSteps();
    }

    private void initManualAuthenticationSteps() {
        List<AuthenticationStep> steps = new ArrayList<>(4);
        steps.add(new ModuleVersionAuthenticationStep(this::processModuleVersionGetting));
        steps.add(new UsernamePasswordAuthenticationStep(this::processLogin));
        steps.add(new PinCodeGeneratorAuthenticationStep(this::processAccessPinSetup));
        steps.add(new OtpStep(this::processOtp, supplementalInformationFormer));
        manualAuthenticationSteps = Collections.unmodifiableList(steps);
    }

    private void initAutoAuthenticationSteps() {
        List<AuthenticationStep> steps = new ArrayList<>(2);
        steps.add(new ModuleVersionAuthenticationStep(this::processModuleVersionGetting));
        steps.add(
                new AutomaticAuthenticationStep(
                        this::processPinAuthentication, "pinAuthentication"));
        autoAuthenticationSteps = Collections.unmodifiableList(steps);
    }

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        if (authContext.isDeviceActivationFinished()) {
            return autoAuthenticationSteps;
        } else {
            return manualAuthenticationSteps;
        }
    }

    private AuthenticationStepResponse processOtp(final String otpCode)
            throws AuthenticationException {
        try {
            AuthenticationResponse response =
                    callLoginRequest(new ConfirmPinByOtpRequest(entityManager, otpCode));
            handleResponse(response, LoginError.NOT_SUPPORTED);
            authContext.finishDeviceActivation();
            return AuthenticationStepResponse.executeNextStep();
        } catch (LoginException ex) {
            authContext.clearAuthData();
            throw ex;
        }
    }

    private AuthenticationStepResponse processLogin(final String username, final String password)
            throws AuthenticationException {
        authContext.setDeviceUUID(UUID.randomUUID().toString());
        LoginRequest request = new LoginRequest(authContext, username, password);
        LoginResponse response = callLoginRequest(request);
        handleResponse(response, LoginError.CREDENTIALS_VERIFICATION_ERROR);
        authContext.setSessionCSRFToken(response.getCsrfToken());
        return AuthenticationStepResponse.executeNextStep();
    }

    private AuthenticationStepResponse processAccessPinSetup(final String accessPin)
            throws AuthenticationException {
        authContext.setAccessPin(accessPin);
        SetupAccessPinRequest request = new SetupAccessPinRequest(authContext);
        SetupAccessPinResponse response = callLoginRequest(request);
        handleResponse(response, LoginError.NOT_SUPPORTED);
        authContext.setMobileChallengeRequestedToken(response.getMobileChallengeRequestedToken());
        return AuthenticationStepResponse.executeNextStep();
    }

    private AuthenticationStepResponse processModuleVersionGetting() throws LoginException {
        authContext.setModuleVersion(callLoginRequest(new ModuleVersionRequest(authContext)));
        return AuthenticationStepResponse.executeNextStep();
    }

    private void handleResponse(final AuthenticationResponse response, final LoginError loginError)
            throws LoginException {
        if (!response.isSuccess()) {
            throw new LoginException(loginError, new LocalizableKey(response.getCode()));
        }
    }

    public AuthenticationStepResponse processPinAuthentication() throws AuthenticationException {
        PinAuthenticationRequest request = new PinAuthenticationRequest(entityManager);
        PinAuthenticationResponse response = callLoginRequest(request);
        authContext.setSessionCSRFToken(response.getCsrfToken());
        try {
            handleResponse(response, LoginError.INCORRECT_CREDENTIALS);
        } catch (LoginException ex) {
            authContext.clearAuthData();
            throw ex;
        }
        return AuthenticationStepResponse.executeNextStep();
    }

    private <T> T callLoginRequest(DefaultRequest<T> request) throws LoginException {
        try {
            return request.call(httpClient);
        } catch (RequestException e) {
            throw new LoginException(LoginError.NOT_SUPPORTED, e.getMessage());
        }
    }
}
