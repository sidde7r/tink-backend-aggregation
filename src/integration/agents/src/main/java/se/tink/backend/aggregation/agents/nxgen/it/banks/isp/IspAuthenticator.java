package se.tink.backend.aggregation.agents.nxgen.it.banks.isp;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.rpc.CheckPinResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.rpc.CheckTimeResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.rpc.RegisterDevice2Response;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.rpc.RegisterDevice3Response;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AutomaticAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.OtpStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.UsernamePasswordAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class IspAuthenticator extends StatelessProgressiveAuthenticator {

    public static final String SESSION_STORAGE_KEY_ACCESS_TOKEN = "access_token";
    private static final String SESSION_STORAGE_KEY_USER_PASSWORD = "user_password";
    private static final String SESSION_STORAGE_KEY_TOTP_MASK = "totp_mask";
    private static final String SESSION_STORAGE_KEY_TOTP_DIGITS = "totp_digits";
    private static final String SESSION_STORAGE_KEY_USERNAME = "username";
    private static final String PERSISTENT_STORAGE_KEY_DEVICE_ID = "device_id";
    private static final String OK_RESPONSE_CODE = "OK";
    public static final String REGISTER_DEVICE_STEP_NAME = "registerDevice";
    public static final String REGISTER_DEVICE_3_STEP_NAME = "registerDevice3";
    public static final String CONFIRM_DEVICE_STEP_NAME = "confirmDevice";

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
                        new UsernamePasswordAuthenticationStep(this::processUsernamePassword),
                        new AutomaticAuthenticationStep(
                                this::registerDevice, REGISTER_DEVICE_STEP_NAME),
                        new OtpStep(this::processOtp, supplementalInformationFormer),
                        new AutomaticAuthenticationStep(
                                this::registerDevice3, REGISTER_DEVICE_3_STEP_NAME),
                        new AutomaticAuthenticationStep(
                                this::confirmDevice, CONFIRM_DEVICE_STEP_NAME));
    }

    private void processUsernamePassword(String username, String password)
            throws AuthenticationException {
        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        apiClient.disableAllBookmark(getDeviceId());
        CheckPinResponse checkPinResponse = apiClient.checkPin(username, password);
        if (!OK_RESPONSE_CODE.equals(checkPinResponse.getExitCode())) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        sessionStorage.put(
                SESSION_STORAGE_KEY_ACCESS_TOKEN, checkPinResponse.getPayload().getAccessToken());
        sessionStorage.put(SESSION_STORAGE_KEY_USER_PASSWORD, password);
    }

    AuthenticationStepResponse registerDevice() throws LoginException {
        if (!OK_RESPONSE_CODE.equals(apiClient.registerDevice().getExitCode())) {
            throw LoginError.REGISTER_DEVICE_ERROR.exception();
        }
        return AuthenticationStepResponse.executeNextStep();
    }

    AuthenticationStepResponse processOtp(String otp) throws LoginException {
        RegisterDevice2Response registerDevice2Response = apiClient.registerDevice2(otp);
        if (!OK_RESPONSE_CODE.equals(registerDevice2Response.getExitCode())) {
            throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
        }
        return AuthenticationStepResponse.executeNextStep();
    }

    AuthenticationStepResponse registerDevice3() {
        String deviceId = getDeviceId();
        RegisterDevice3Response registerDevice3Response =
                apiClient.registerDevice3(
                        IspConstants.DEVICE_NAME,
                        deviceId,
                        sessionStorage.get(SESSION_STORAGE_KEY_USER_PASSWORD));
        String seedXml = registerDevice3Response.getPayload().getSeed();
        if (seedXml == null) {
            throw new IllegalStateException("OTP seed null");
        }
        TotpSeedXmlHelper.validateTotpType(seedXml);
        sessionStorage.put(SESSION_STORAGE_KEY_TOTP_MASK, TotpSeedXmlHelper.getTotpMask(seedXml));
        sessionStorage.put(
                SESSION_STORAGE_KEY_TOTP_DIGITS, TotpSeedXmlHelper.getTotpDigits(seedXml));
        return AuthenticationStepResponse.executeNextStep();
    }

    AuthenticationStepResponse confirmDevice() {
        int digits = Integer.parseInt(sessionStorage.get(SESSION_STORAGE_KEY_TOTP_DIGITS));
        String mask = sessionStorage.get(SESSION_STORAGE_KEY_TOTP_MASK);
        String password = sessionStorage.get(SESSION_STORAGE_KEY_USER_PASSWORD);
        String username = sessionStorage.get(SESSION_STORAGE_KEY_USERNAME);
        String deviceId = getDeviceId();
        CheckTimeResponse response = apiClient.checkTime(username, deviceId, Instant.now());
        long currentTime =
                Instant.now().getEpochSecond()
                        - Integer.parseInt(response.getPayload().getDifferenceInSeconds());
        String totp = TotpCalculator.calculateTOTP(digits, mask, password, currentTime);
        apiClient.confirmDevice(deviceId, totp);
        return AuthenticationStepResponse.executeNextStep();
    }

    String getDeviceId() {
        String deviceId = persistentStorage.get(PERSISTENT_STORAGE_KEY_DEVICE_ID);
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString().toUpperCase();
            persistentStorage.put(PERSISTENT_STORAGE_KEY_DEVICE_ID, deviceId);
        }
        return deviceId;
    }

    @Override
    public boolean isManualAuthentication(CredentialsRequest request) {
        return true; // TODO: change once automatic flow is implemented
    }
}
