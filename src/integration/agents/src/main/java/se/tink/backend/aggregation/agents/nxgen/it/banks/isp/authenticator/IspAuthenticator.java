package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator;

import static java.util.Objects.requireNonNull;
import static se.tink.backend.aggregation.agents.nxgen.it.banks.isp.IspConstants.StorageKeys;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.apiclient.IspApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.rpc.CheckPinResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.rpc.CheckTimeResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.rpc.RegisterDevice3Response;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AutomaticAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.OtpStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.UsernamePasswordAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class IspAuthenticator extends StatelessProgressiveAuthenticator {

    private static final String REGISTER_DEVICE_STEP_NAME = "registerDevice";
    private static final String REGISTER_DEVICE_3_STEP_NAME = "registerDevice3";
    private static final String CONFIRM_DEVICE_STEP_NAME = "confirmDevice";
    private static final String CHECK_PIN_STEP_NAME = "checkPin";
    private static final String LOGIN_ERROR = "LOGIN_ERROR";

    private final IspApiClient apiClient;
    private final SupplementalInformationFormer supplementalInformationFormer;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;
    private List<AuthenticationStep> manualAuthenticationSteps;

    public IspAuthenticator(
            final IspApiClient apiClient,
            final SupplementalInformationFormer supplementalInformationFormer,
            final SessionStorage sessionStorage,
            final PersistentStorage persistentStorage) {
        this.apiClient = requireNonNull(apiClient);
        this.supplementalInformationFormer = requireNonNull(supplementalInformationFormer);
        this.sessionStorage = requireNonNull(sessionStorage);
        this.persistentStorage = requireNonNull(persistentStorage);
        initManualAuthenticationSteps();
    }

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        return manualAuthenticationSteps;
    }

    private void initManualAuthenticationSteps() {

        manualAuthenticationSteps =
                ImmutableList.of(
                        new UsernamePasswordAuthenticationStep(
                                this::processUsernamePassword, CHECK_PIN_STEP_NAME),
                        new AutomaticAuthenticationStep(
                                this::registerDevice, REGISTER_DEVICE_STEP_NAME),
                        new OtpStep(this::processOtp, supplementalInformationFormer),
                        new UsernamePasswordAuthenticationStep(
                                this::registerDevice3, REGISTER_DEVICE_3_STEP_NAME),
                        new UsernamePasswordAuthenticationStep(
                                this::confirmDevice, CONFIRM_DEVICE_STEP_NAME));
    }

    private void processUsernamePassword(String username, String password)
            throws AuthenticationException {
        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        try {
            CheckPinResponse checkPinResponse = apiClient.checkPin(username, password);
            sessionStorage.put(
                    StorageKeys.ACCESS_TOKEN, checkPinResponse.getPayload().getAccessToken());
        } catch (HttpResponseException e) {
            ErrorResponse errorResponse = e.getResponse().getBody(ErrorResponse.class);
            if (errorResponse != null && LOGIN_ERROR.equals(errorResponse.getMessage())) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }
            throw e;
        }
    }

    private AuthenticationStepResponse registerDevice() throws LoginException {
        if (!apiClient.registerDevice().isOk()) {
            throw LoginError.REGISTER_DEVICE_ERROR.exception();
        }
        return AuthenticationStepResponse.executeNextStep();
    }

    AuthenticationStepResponse processOtp(String otp) throws LoginException {
        try {
            apiClient.registerDevice2(otp);
        } catch (HttpResponseException e) {
            ErrorResponse errorResponse = e.getResponse().getBody(ErrorResponse.class);
            if (errorResponse != null && LOGIN_ERROR.equals(errorResponse.getMessage())) {
                throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
            }
            throw e;
        }

        return AuthenticationStepResponse.executeNextStep();
    }

    AuthenticationStepResponse registerDevice3(String username, String password) {
        RegisterDevice3Response registerDevice3Response =
                apiClient.registerDevice3(getDeviceId(), password);
        String seedXml = registerDevice3Response.getPayload().getSeed();
        if (seedXml == null) {
            throw new IllegalStateException("OTP seed null");
        }
        TotpSeedXmlHelper.validateTotpType(seedXml);
        sessionStorage.put(StorageKeys.TOTP_MASK, TotpSeedXmlHelper.getTotpMask(seedXml));
        sessionStorage.put(StorageKeys.TOTP_DIGITS, TotpSeedXmlHelper.getTotpDigits(seedXml));
        sessionStorage.put(
                StorageKeys.TRANSACTION_ID,
                registerDevice3Response.getPayload().getTransactionId());
        return AuthenticationStepResponse.executeNextStep();
    }

    AuthenticationStepResponse confirmDevice(String username, String password) {
        int digits = Integer.parseInt(sessionStorage.get(StorageKeys.TOTP_DIGITS));
        String mask = sessionStorage.get(StorageKeys.TOTP_MASK);
        String transactionId = sessionStorage.get(StorageKeys.TRANSACTION_ID);
        String deviceId = getDeviceId();
        LocalDateTime now = LocalDateTime.now();
        CheckTimeResponse response = apiClient.checkTime(username, deviceId, LocalDateTime.now());
        long currentTime =
                now.toEpochSecond(ZoneOffset.UTC)
                        - Long.parseLong(response.getPayload().getDifferenceInSeconds());
        String totp = TotpCalculator.calculateTOTP(digits, mask, password, currentTime);
        apiClient.confirmDevice(deviceId, totp, transactionId);
        return AuthenticationStepResponse.authenticationSucceeded();
    }

    String getDeviceId() {
        String deviceId = persistentStorage.get(StorageKeys.DEVICE_ID);
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString().toUpperCase();
            persistentStorage.put(StorageKeys.DEVICE_ID, deviceId);
        }
        return deviceId;
    }

    @Override
    public boolean isManualAuthentication(CredentialsRequest request) {
        return true;
    }
}
