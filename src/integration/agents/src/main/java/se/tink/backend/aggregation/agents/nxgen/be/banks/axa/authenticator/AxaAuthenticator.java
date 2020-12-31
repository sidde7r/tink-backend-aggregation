package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.errorhandling.ResponseErrorHandler;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.errorhandling.ResponseErrorHandlingBuilder;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.AnonymousInvokeRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.AnonymousInvokeResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.AssertFormRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.AssertFormResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.BindRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.BindResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.LogonResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.utils.AxaCryptoUtil;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AutomaticAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;

public class AxaAuthenticator extends StatelessProgressiveAuthenticator {

    private static final String LOGON_FINAL_STEP = "logonFinalStep";

    private final AxaStorage storage;
    private final AxaApiClient apiClient;
    private final SupplementalInformationFormer supplementalInformationFormer;
    private final AxaAuthenticatorRequestCreator requestCreator;
    private final List<AuthenticationStep> manualAuthenticationSteps;
    private final List<AuthenticationStep> autoAuthenticationSteps;
    private final ResponseErrorHandler errorHandler;

    public AxaAuthenticator(
            AxaStorage axaStorage,
            AxaApiClient apiClient,
            SupplementalInformationFormer supplementalInformationFormer) {
        this.storage = initStorageData(axaStorage);
        this.apiClient = apiClient;
        this.supplementalInformationFormer = supplementalInformationFormer;
        this.manualAuthenticationSteps = initManualAuthenticationSteps();
        this.autoAuthenticationSteps = initAutoAuthenticationSteps();
        this.requestCreator = new AxaAuthenticatorRequestCreator(storage);
        this.errorHandler = ResponseErrorHandlingBuilder.DEFAULT_CHAIN;
    }

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        return storage.isDeviceRegistered() ? autoAuthenticationSteps : manualAuthenticationSteps;
    }

    private AxaStorage initStorageData(AxaStorage axaStorage) {
        if (!axaStorage.isDeviceRegistered()) {
            KeyPair rsaKeyPair = AxaCryptoUtil.generateRSAKeyPair();
            KeyPair challengeSignECKeyPair = AxaCryptoUtil.generateChallengeSignECKeyPair();
            KeyPair requestSignatureECKeyPair = AxaCryptoUtil.generateRequestSignatureECKeyPair();
            String deviceId = UUID.randomUUID().toString().toUpperCase();
            String deviceName = UUID.randomUUID().toString().toLowerCase().replace("-", "");
            String batchInstallationId = UUID.randomUUID().toString().toUpperCase();

            axaStorage.storeRSAKeyPair(rsaKeyPair);
            axaStorage.storeChallengeSignECKeyPair(challengeSignECKeyPair);
            axaStorage.storeRequestSignatureECKeyPair(requestSignatureECKeyPair);
            axaStorage.storeDeviceId(deviceId);
            axaStorage.storeDeviceName(deviceName);
            axaStorage.storeBatchInstallationId(batchInstallationId);
        }

        String paramsSessionId = UUID.randomUUID().toString().toLowerCase();
        axaStorage.storeParamsSessionId(paramsSessionId);

        return axaStorage;
    }

    private List<AuthenticationStep> initManualAuthenticationSteps() {
        List<AuthenticationStep> steps = new ArrayList<>(11);
        steps.add(
                new AutomaticAuthenticationStep(
                        this::processAnonymousInvoke, "anonymousInvokeStep"));
        steps.add(new AutomaticAuthenticationStep(this::processUCRAssertForm, "ucrAssertFormStep"));
        steps.add(
                new AutomaticAuthenticationStep(
                        this::processCardNumberAssertForm, "cardNumberAssertFormStep"));
        steps.add(new AutomaticAuthenticationStep(this::processBind, "bindStep"));
        steps.add(new AxaLoginAuthenticationStep(supplementalInformationFormer, storage));
        steps.add(new AutomaticAuthenticationStep(this::processFirstOtp, "firstOtpStep"));
        steps.add(
                new AutomaticAuthenticationStep(
                        this::processProfileNameAssertForm, "profileNameAssertFormStep"));
        steps.add(new AxaSigningAuthenticationStep(supplementalInformationFormer, storage));
        steps.add(new AutomaticAuthenticationStep(this::processSecondOtp, "secondOtpStep"));
        steps.add(
                new AutomaticAuthenticationStep(
                        this::processAssertRegistration, "assertRegistrationStep"));
        steps.add(
                new AutomaticAuthenticationStep(
                        this::processAssertConfirmation, "assertConfirmationStep"));
        steps.add(new AutomaticAuthenticationStep(this::processLegacyLogon, LOGON_FINAL_STEP));
        return steps;
    }

    private List<AuthenticationStep> initAutoAuthenticationSteps() {
        List<AuthenticationStep> steps = new ArrayList<>(3);
        steps.add(new AutomaticAuthenticationStep(this::processLogin, "initialLoginStep"));
        steps.add(
                new AutomaticAuthenticationStep(
                        this::processAssertPinAuthentication, "assertPinAuthenticationStep"));
        steps.add(new AutomaticAuthenticationStep(this::processLegacyLogon, LOGON_FINAL_STEP));
        return steps;
    }

    private AuthenticationStepResponse processAnonymousInvoke() {
        AnonymousInvokeRequest request = requestCreator.createAnonymousInvokeRequest();
        AnonymousInvokeResponse response = apiClient.anonymousInvoke(request);
        verifyWithStorageCleanup(response);
        storage.storeValues(response);
        return AuthenticationStepResponse.executeNextStep();
    }

    private AuthenticationStepResponse processUCRAssertForm() {
        AssertFormRequest request = requestCreator.createUCRAssertFormRequest();
        AssertFormResponse response = apiClient.assertForm(request);
        verifyWithStorageCleanup(response);
        storage.storeValuesFromUCRAssertFormResponse(response);
        return AuthenticationStepResponse.executeNextStep();
    }

    private AuthenticationStepResponse processCardNumberAssertForm() {
        AssertFormRequest request = requestCreator.createCardNumberAssertFormRequest();
        AssertFormResponse response = apiClient.assertForm(request);
        verifyWithStorageCleanup(response);
        storage.storeValuesFromCardNumberAssertFormResponse(response);
        return AuthenticationStepResponse.executeNextStep();
    }

    private AuthenticationStepResponse processFirstOtp() {
        storage.storeValuesFromFirstOtpResponse(sendOtpRequest(true));
        return AuthenticationStepResponse.executeNextStep();
    }

    private AuthenticationStepResponse processBind() {
        BindRequest request = requestCreator.createBindRequest();
        BindResponse response = apiClient.bind(request);
        verifyWithStorageCleanup(response);
        storage.storeValuesFromBindResponse(response);
        return AuthenticationStepResponse.executeNextStep();
    }

    private AuthenticationStepResponse processProfileNameAssertForm() {
        AssertFormRequest request = requestCreator.createProfileNameAssertFormRequest();
        AssertFormResponse response = apiClient.assertFormWithSignature(request);
        verifyWithStorageCleanup(response);
        storage.storeValuesFromProfileNameAssertFormResponse(response);
        return AuthenticationStepResponse.executeNextStep();
    }

    private AuthenticationStepResponse processSecondOtp() {
        storage.storeValuesFromSecondOtpResponse(sendOtpRequest(true));
        return AuthenticationStepResponse.executeNextStep();
    }

    private AuthenticationStepResponse processAssertRegistration() {
        AssertFormRequest request = requestCreator.createAssertRegistrationRequest();
        AssertFormResponse response = apiClient.assertFormWithSignature(request);
        verifyWithStorageCleanup(response);
        storage.storeValuesFromAssertRegistrationResponse(response);
        return executeNextStep(response);
    }

    private AuthenticationStepResponse processAssertConfirmation() {
        AssertFormRequest request = requestCreator.createAssertConfirmationRequest();
        AssertFormResponse response = apiClient.assertFormWithSignature(request);
        verifyWithStorageCleanup(response);
        storage.storeValuesFromAssertConfirmationResponse(response);
        return executeNextStep(response);
    }

    private AuthenticationStepResponse processLegacyLogon() {
        LogonResponse response = apiClient.postLogon();
        storage.storeValuesFromLogonResponse(response);
        return AuthenticationStepResponse.authenticationSucceeded();
    }

    private AuthenticationStepResponse processLogin() {
        LoginRequest request = requestCreator.createLoginRequest();
        LoginResponse response = apiClient.login(request);
        verifyWithStorageCleanup(response);
        storage.storeValuesFromLoginResponse(response);
        return AuthenticationStepResponse.executeNextStep();
    }

    private AuthenticationStepResponse processAssertPinAuthentication() {
        AssertFormRequest request = requestCreator.createAssertPinAuthenticationRequest();
        AssertFormResponse response = apiClient.assertFormWithSignature(request);
        verifyWithStorageCleanup(response);
        storage.storeValuesFromAssertPinAuthenticationResponse(response);
        return AuthenticationStepResponse.executeNextStep();
    }

    private AssertFormResponse sendOtpRequest(boolean withSignature) {
        AssertFormRequest request = requestCreator.createAssertAuthenticationRequest();
        AssertFormResponse response =
                withSignature
                        ? apiClient.assertFormWithSignature(request)
                        : apiClient.assertForm(request);
        verifyWithStorageCleanup(response);
        return response;
    }

    private void verifyWithStorageCleanup(BaseResponse<?> response) {
        try {
            errorHandler.handleError(response);
        } catch (LoginException | BankServiceException ex) {
            storage.clear();
            throw ex;
        }
    }

    private AuthenticationStepResponse executeNextStep(AssertFormResponse response) {
        if ("completed".equals(response.getData().getState())) {
            storage.registerDevice();
            return AuthenticationStepResponse.executeStepWithId(LOGON_FINAL_STEP);
        }

        return AuthenticationStepResponse.executeNextStep();
    }
}
