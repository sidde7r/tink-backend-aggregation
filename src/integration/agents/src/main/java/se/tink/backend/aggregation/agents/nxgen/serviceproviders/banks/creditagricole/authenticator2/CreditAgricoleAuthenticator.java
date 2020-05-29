package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator2;

import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants.ErrorCode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants.StorageKey;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.AccessibilityGridResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.AuthenticateRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.AuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.CreateUserRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.DefaultAuthRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.FindProfilesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.FindProfilesResponse.ActiveUserList;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.OtpAuthResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.OtpSmsRequest;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AutomaticAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.OtpStep;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

// TODO: remove old authenticator and move this to proper package
public class CreditAgricoleAuthenticator extends StatelessProgressiveAuthenticator {

    private final CreditAgricoleApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final SupplementalInformationFormer supplementalInformationFormer;
    private final List<AuthenticationStep> manualAuthenticationSteps;
    private final List<AuthenticationStep> autoAuthenticationSteps;

    public CreditAgricoleAuthenticator(
            CreditAgricoleApiClient apiClient,
            PersistentStorage persistentStorage,
            SupplementalInformationFormer supplementalInformationFormer) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.supplementalInformationFormer = supplementalInformationFormer;
        this.manualAuthenticationSteps = initManualAuthSteps();
        this.autoAuthenticationSteps = initAutoAuthSteps();
    }

    @Override
    public List<? extends AuthenticationStep> authenticationSteps() {
        return isDeviceRegistered() ? autoAuthenticationSteps : manualAuthenticationSteps;
    }

    @Override
    public boolean isManualAuthentication(CredentialsRequest request) {
        return !isDeviceRegistered();
    }

    private List<AuthenticationStep> initManualAuthSteps() {
        List<AuthenticationStep> steps = new ArrayList<>(5);
        steps.add(
                new AutomaticAuthenticationStep(
                        this::processAccessibilityGrid, "processAccessibilityGrid"));
        steps.add(new AutomaticAuthenticationStep(this::processCreateUser, "processCreatingUser"));
        steps.add(new OtpStep(this::processOtp, supplementalInformationFormer));
        steps.add(new AutomaticAuthenticationStep(this::processProfile, "processCreateProfile"));
        steps.add(new AutomaticAuthenticationStep(this::processPrimaryAuth, "processPrimaryAuth"));
        return steps;
    }

    private List<AuthenticationStep> initAutoAuthSteps() {
        List<AuthenticationStep> steps = new ArrayList<>(3);
        steps.add(
                new AutomaticAuthenticationStep(
                        this::processLoginWithToken, "processLoginWithToken"));
        steps.add(
                new AutomaticAuthenticationStep(
                        this::processAccessibilityGrid, "processAccessibilityGrid"));
        steps.add(
                new AutomaticAuthenticationStep(
                        this::processLoginWithAccountCode, "processLoginWithAccountCode"));
        return steps;
    }

    private AuthenticationStepResponse processAccessibilityGrid() {
        AccessibilityGridResponse response = apiClient.getAccessibilityGrid();
        persistentStorage.put(StorageKey.NUMPAD_SEQUENCE, response.getSequence());
        persistentStorage.put(StorageKey.PUBLIC_KEY, response.getPublicKey());
        return AuthenticationStepResponse.executeNextStep();
    }

    private AuthenticationStepResponse processCreateUser() {
        CreateUserRequest request =
                new CreateUserRequest(
                        createEncryptedAccountCode(),
                        persistentStorage.get(StorageKey.USER_ACCOUNT_NUMBER));
        apiClient.createUser(request);
        return AuthenticationStepResponse.executeNextStep();
    }

    private AuthenticationStepResponse processOtp(String otpCode) {
        apiClient.requestOtp(new DefaultAuthRequest("create_user"));
        OtpSmsRequest request = new OtpSmsRequest(otpCode);
        OtpAuthResponse response = apiClient.sendOtpCode(request);
        persistentStorage.put(StorageKey.LL_TOKEN, response.getLlToken());
        persistentStorage.put(StorageKey.SL_TOKEN, response.getSlToken());
        return AuthenticationStepResponse.executeNextStep();
    }

    private AuthenticationStepResponse processProfile() {
        FindProfilesResponse profilesResponse = apiClient.findProfiles();
        String email = persistentStorage.get(StorageKey.LOGIN_EMAIL);

        ActiveUserList activeUserProfile =
                profilesResponse.getActiveUsersList().stream()
                        .filter(activeUserList -> email.equals(activeUserList.getUserEmail()))
                        .findFirst()
                        .orElseThrow(RuntimeException::new);

        persistentStorage.put(StorageKey.PARTNER_ID, activeUserProfile.getPartnerId());
        persistentStorage.put(StorageKey.USER_ID, activeUserProfile.getUserId());

        apiClient.restoreProfile(persistentStorage.get(StorageKey.APP_CODE));
        return AuthenticationStepResponse.executeNextStep();
    }

    private AuthenticationStepResponse processPrimaryAuth() {
        AuthenticateRequest request =
                AuthenticateRequest.createPrimaryAuthRequest(
                        createEncryptedAccountCode(),
                        persistentStorage.get(StorageKey.USER_ID),
                        persistentStorage.get(StorageKey.USER_ACCOUNT_NUMBER));
        processFinalLoginStep(request);
        persistentStorage.put(StorageKey.IS_DEVICE_REGISTERED, true);
        return AuthenticationStepResponse.authenticationSucceeded();
    }

    private AuthenticationStepResponse processLoginWithToken() {
        AuthenticateRequest request =
                AuthenticateRequest.createTokenLoginRequest(
                        persistentStorage.get(StorageKey.LL_TOKEN),
                        persistentStorage.get(StorageKey.USER_ID));
        AuthenticateResponse response = apiClient.authenticate(request);

        if (!response.isResponseOK()) {
            if (response.getAllErrorCodes().contains(ErrorCode.PASSWORD_AUTH_REQUIRED)) {
                return AuthenticationStepResponse.executeNextStep();
            }

            throw new RuntimeException("Couldn't login, errors: " + response.getAllErrorCodes());
        }

        return AuthenticationStepResponse.authenticationSucceeded();
    }

    private AuthenticationStepResponse processLoginWithAccountCode() {
        AuthenticateRequest request =
                AuthenticateRequest.createPasswordLoginRequest(
                        createEncryptedAccountCode(),
                        persistentStorage.get(StorageKey.USER_ID),
                        persistentStorage.get(StorageKey.USER_ACCOUNT_NUMBER));
        processFinalLoginStep(request);
        return AuthenticationStepResponse.authenticationSucceeded();
    }

    private AuthenticateResponse processFinalLoginStep(AuthenticateRequest request) {
        AuthenticateResponse response = apiClient.authenticate(request);
        persistentStorage.put(StorageKey.LL_TOKEN, response.getLlToken());
        persistentStorage.put(StorageKey.SL_TOKEN, response.getSlToken());
        persistentStorage.put(StorageKey.PARTNER_ID, response.getPerimeterId());
        persistentStorage.put(StorageKey.USER_ID, response.getUserId());
        return response;
    }

    private String createEncryptedAccountCode() {
        String mappedAccountCode =
                mapAccountCodeToNumpadSequence(persistentStorage.get(StorageKey.USER_ACCOUNT_CODE));
        RSAPublicKey publicKey =
                RSA.getPubKeyFromBytes(
                        EncodingUtils.decodeBase64String(
                                persistentStorage.get(StorageKey.PUBLIC_KEY)));
        byte[] encryptedAccountCode = RSA.encryptNonePkcs1(publicKey, mappedAccountCode.getBytes());
        return EncodingUtils.encodeAsBase64String(encryptedAccountCode);
    }

    private String mapAccountCodeToNumpadSequence(String realAccountCode) {
        String numpadSequenceWithoutDelimiter =
                persistentStorage.get(StorageKey.NUMPAD_SEQUENCE).replace(";", "");

        return Arrays.stream(realAccountCode.split(""))
                .map(numpadSequenceWithoutDelimiter::indexOf)
                .map(String::valueOf)
                .collect(Collectors.joining(";"));
    }

    private boolean isDeviceRegistered() {
        return persistentStorage
                .get(StorageKey.IS_DEVICE_REGISTERED, Boolean.class)
                .orElse(Boolean.FALSE);
    }
}
