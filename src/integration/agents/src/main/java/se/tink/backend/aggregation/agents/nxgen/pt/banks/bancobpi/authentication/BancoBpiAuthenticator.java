package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.request.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.request.ConfirmPinByOtpRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.request.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.request.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.request.PinAuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.request.SetupAccessPinRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.request.SetupAccessPinResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AutomaticAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.OtpStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.PinCodeGeneratorAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.UsernamePasswordAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.i18n.LocalizableKey;

public class BancoBpiAuthenticator extends StatelessProgressiveAuthenticator {

    private static final String PERSISTENCE_STORAGE_KEY = "BancoBpiUserState";

    private List<AuthenticationStep> autoAuthenticationSteps;
    private List<AuthenticationStep> manualAuthenticationSteps;
    private TinkHttpClient httpClient;
    private final SupplementalInformationFormer supplementalInformationFormer;
    private boolean manualAuthenticationFlag = true;
    private BancoBpiUserState userState;
    private SessionStorage sessionStorage;
    private static final Gson gson = new Gson();

    public BancoBpiAuthenticator(
            final TinkHttpClient httpClient,
            final SupplementalInformationFormer supplementalInformationFormer,
            SessionStorage sessionStorage) {
        this.httpClient = httpClient;
        this.supplementalInformationFormer = supplementalInformationFormer;
        this.sessionStorage = sessionStorage;
        loadUserState();
        initManualAuthenticationSteps();
        initAutoAuthenticationSteps();
    }

    private void initManualAuthenticationSteps() {
        List<AuthenticationStep> steps = new ArrayList<>(3);
        steps.add(new UsernamePasswordAuthenticationStep(this::processLogin));
        steps.add(new PinCodeGeneratorAuthenticationStep(this::processAccessPinSetup));
        steps.add(new OtpStep(this::processOtp, supplementalInformationFormer));
        manualAuthenticationSteps = Collections.unmodifiableList(steps);
    }

    private void initAutoAuthenticationSteps() {
        List<AuthenticationStep> steps = new ArrayList<>(1);
        steps.add(new AutomaticAuthenticationStep(this::processPinAuthentication));
        autoAuthenticationSteps = Collections.unmodifiableList(steps);
    }

    @Override
    public SteppableAuthenticationResponse processAuthentication(
            SteppableAuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        SteppableAuthenticationResponse response = super.processAuthentication(request);
        saveUserState();
        return response;
    }

    @Override
    public Iterable<? extends AuthenticationStep> authenticationSteps()
            throws AuthenticationException, AuthorizationException {
        if (userState.isDeviceActivationFinished()) {
            manualAuthenticationFlag = false;
            return autoAuthenticationSteps;
        } else {
            manualAuthenticationFlag = true;
            return manualAuthenticationSteps;
        }
    }

    private void processOtp(final String otpCode) throws AuthenticationException {
        try {
            ConfirmPinByOtpRequest request = new ConfirmPinByOtpRequest(userState, otpCode);
            handleResponse(request.call(httpClient), LoginError.NOT_SUPPORTED);
            userState.finishDeviceActivation();
        } catch (LoginException ex) {
            userState.clearAuthData();
            throw ex;
        }
    }

    private void processLogin(final String username, final String password)
            throws AuthenticationException {
        userState.setDeviceUUID(UUID.randomUUID().toString());
        LoginRequest request = new LoginRequest(userState.getDeviceUUID(), username, password);
        LoginResponse response = request.call(httpClient);
        handleResponse(response, LoginError.CREDENTIALS_VERIFICATION_ERROR);
        userState.setSessionCSRFToken(response.getCsrfToken());
    }

    private void processAccessPinSetup(final String accessPin) throws AuthenticationException {
        userState.setAccessPin(accessPin);
        SetupAccessPinRequest request = new SetupAccessPinRequest(userState);
        SetupAccessPinResponse response = request.call(httpClient);
        handleResponse(response, LoginError.NOT_SUPPORTED);
        userState.setMobileChallengeRequestedToken(response.getMobileChallengeRequestedToken());
    }

    private void handleResponse(final AuthenticationResponse response, final LoginError loginError)
            throws LoginException {
        if (!response.isSuccess()) {
            throw new LoginException(loginError, new LocalizableKey(response.getCode()));
        }
    }

    public void processPinAuthentication() throws AuthenticationException {
        PinAuthenticationRequest request = new PinAuthenticationRequest(userState);
        LoginResponse response = request.call(httpClient);
        userState.setSessionCSRFToken(response.getCsrfToken());
        try {
            handleResponse(response, LoginError.INCORRECT_CREDENTIALS);
        } catch (LoginException ex) {
            userState.clearAuthData();
            throw ex;
        }
    }

    @Override
    public boolean isManualAuthentication(CredentialsRequest request) {
        return manualAuthenticationFlag;
    }

    private void loadUserState() {
        userState =
                gson.fromJson(sessionStorage.get(PERSISTENCE_STORAGE_KEY), BancoBpiUserState.class);
    }

    private void saveUserState() {
        sessionStorage.put(PERSISTENCE_STORAGE_KEY, gson.toJson(userState));
    }
}
