package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants.AuthenticationParams;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants.CaixaPayloadValues;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants.TemporaryStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants.UserData;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.entities.PinScaEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.entities.SmsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.LoginResultResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.ScaResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.step.CodeCardStep;
import se.tink.backend.aggregation.agents.utils.crypto.LaCaixaPasswordHash;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload.Android;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload.Desktop;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload.Ios;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AutomaticAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.OtpStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.UsernamePasswordAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.i18n.Catalog;

@Slf4j
public class LaCaixaManualAuthenticator {
    private final LaCaixaApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final LogMasker logMasker;
    private final Storage authStorage;
    private final Credentials credentials;
    private final SupplementalInformationHelper supplementalInformationHelper;

    // steps
    private final AuthenticationStep otpStep;
    private final AuthenticationStep codeCardStep;
    private final AuthenticationStep appStep;
    private final AuthenticationStep finalizeStep;

    public LaCaixaManualAuthenticator(
            LaCaixaApiClient apiClient,
            PersistentStorage persistentStorage,
            LogMasker logMasker,
            SupplementalInformationFormer supplementalInformationFormer,
            Storage authStorage,
            Catalog catalog,
            Credentials credentials,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.logMasker = logMasker;
        this.authStorage = authStorage;
        this.credentials = credentials;
        this.supplementalInformationHelper = supplementalInformationHelper;

        otpStep = new OtpStep(this::processOtp, supplementalInformationFormer);
        appStep = new AutomaticAuthenticationStep(this::handleAppSca, "caixabankSign");
        finalizeStep =
                new AutomaticAuthenticationStep(this::finalizeEnrolment, "finalizeEnrolment");
        codeCardStep = new CodeCardStep(catalog, authStorage);
    }

    public List<AuthenticationStep> getAuthenticationSteps() {
        return ImmutableList.of(
                new UsernamePasswordAuthenticationStep(this::login),
                new AutomaticAuthenticationStep(this::initiateEnrolment, "initiateEnrolment"),
                appStep,
                codeCardStep,
                finalizeStep,
                otpStep,
                finalizeStep);
    }

    private AuthenticationStepResponse login(String username, String password)
            throws LoginException {
        // Requests a session ID from the server in the form of a cookie.
        // Also gets seed for password hashing.
        SessionResponse sessionResponse = apiClient.initializeSession();

        final String pin =
                LaCaixaPasswordHash.hash(
                        sessionResponse.getSeed(), sessionResponse.getIterations(), password);
        logMasker.addNewSensitiveValuesToMasker(Collections.singleton(pin));

        LoginRequest loginRequest = new LoginRequest(username, pin);

        LoginResultResponse loginResultResponse = apiClient.checkLoginResult(loginRequest);

        if (!"OK".equalsIgnoreCase(loginResultResponse.getLoginResultInfo())) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        String isScaNeeded = apiClient.checkIfScaNeeded();

        if ("N".equalsIgnoreCase(isScaNeeded)) {
            return AuthenticationStepResponse.authenticationSucceeded();
        }

        // Construct login request from username and hashed password
        return AuthenticationStepResponse.executeNextStep();
    }

    private AuthenticationStepResponse initiateEnrolment() {
        // only ask for SCA on manual authentication
        if (persistentStorage.get(UserData.IS_CAIXA_MANUAL_DONE) != null) {
            log.info("Skipping enrolment - not a manual authentication");
            return AuthenticationStepResponse.authenticationSucceeded();
        }

        // ask for SCA
        log.info("Initiating enrolment");
        final ScaResponse response = apiClient.initiateEnrolment();
        return handleScaResponse(response);
    }

    private AuthenticationStepResponse handleScaResponse(ScaResponse response) {
        log.info("SCA Type: " + response.getScaType());
        switch (response.getScaType().toUpperCase()) {
            case AuthenticationParams.SCA_TYPE_APP:
                // SCA with CaixaBank Sign app
                return AuthenticationStepResponse.executeStepWithId(appStep.getIdentifier());
            case AuthenticationParams.SCA_TYPE_PIN:
                // Password again
                return handlePasswordSca(response.getPin1Sca());
            case AuthenticationParams.SCA_TYPE_PIN_BANKIA:
                // Password again
                return handlePasswordSca(response.getPin2ScaBankia());
            case AuthenticationParams.SCA_TYPE_SMS:
                // SCA with OTP SMS
                authStorage.put(TemporaryStorage.SCA_SMS, response.getSms());
                return AuthenticationStepResponse.executeStepWithId(otpStep.getIdentifier());
            case AuthenticationParams.SCA_TYPE_CODECARD:
                // SCA with code card
                authStorage.put(TemporaryStorage.CODE_CARD, response.getCodeCard());
                return AuthenticationStepResponse.executeStepWithId(codeCardStep.getIdentifier());
            default:
                throw new IllegalStateException("Unknown SCA type: " + response.getScaType());
        }
    }

    private AuthenticationStepResponse handlePasswordSca(PinScaEntity pinSca) {
        final String password = credentials.getField(Key.PASSWORD);
        final String code =
                LaCaixaPasswordHash.hash(pinSca.getSeed(), pinSca.getIterations(), password);
        logMasker.addNewSensitiveValuesToMasker(Collections.singleton(code));
        final ScaResponse response = apiClient.authorizeSCA(code);
        return handleScaResponse(response);
    }

    private AuthenticationStepResponse processOtp(String otp) {
        log.info("Sending OTP");
        final SmsEntity smsData =
                authStorage
                        .get(TemporaryStorage.SCA_SMS, SmsEntity.class)
                        .orElseThrow(() -> new IllegalStateException("SCA SMS Entity not found"));
        final String code =
                LaCaixaPasswordHash.hash(smsData.getSeed(), smsData.getIterations(), otp);
        authStorage.put(TemporaryStorage.ENROLMENT_CODE, code);
        return AuthenticationStepResponse.executeNextStep();
    }

    private AuthenticationStepResponse finalizeEnrolment() {
        final String code = Strings.nullToEmpty(authStorage.get(TemporaryStorage.ENROLMENT_CODE));
        logMasker.addNewSensitiveValuesToMasker(Collections.singleton(code));
        apiClient.finalizeEnrolment(code);

        persistentStorage.put(UserData.IS_CAIXA_MANUAL_DONE, true);
        return AuthenticationStepResponse.authenticationSucceeded();
    }

    private AuthenticationStepResponse handleAppSca() {
        log.info("Waiting for app signature");
        apiClient.initiateAuthSignature();

        // launch signing app
        supplementalInformationHelper.openThirdPartyApp(
                createAppPayload(new URL(AuthenticationParams.SIGNING_URL)));
        waitForAppSign();

        authStorage.put(TemporaryStorage.ENROLMENT_CODE, "");
        return AuthenticationStepResponse.executeStepWithId(finalizeStep.getIdentifier());
    }

    private void waitForAppSign() {
        // poll
        final Retryer<Boolean> signPoller =
                RetryerBuilder.<Boolean>newBuilder()
                        .retryIfResult(isOperationSigned -> !isOperationSigned)
                        .withWaitStrategy(WaitStrategies.fixedWait(2, TimeUnit.SECONDS))
                        .withStopStrategy(StopStrategies.stopAfterAttempt(90))
                        .build();

        try {
            signPoller.call(() -> apiClient.verifyAuthSignature().isOperationSigned());
        } catch (RetryException e) {
            throw ThirdPartyAppError.TIMED_OUT.exception("Caixa Sign SCA timed out.");
        } catch (ExecutionException e) {
            throw LoginError.DEFAULT_MESSAGE.exception();
        }
    }

    private ThirdPartyAppAuthenticationPayload createAppPayload(URL url) {
        Preconditions.checkNotNull(url, "URL must not be null.");
        ThirdPartyAppAuthenticationPayload payload = new ThirdPartyAppAuthenticationPayload();
        Android androidPayload = new Android();
        androidPayload.setIntent(url.get());
        androidPayload.setPackageName(CaixaPayloadValues.ANDROID_PACKAGE_NAME);

        Ios iOsPayload = new Ios();
        iOsPayload.setAppScheme(url.getScheme());
        iOsPayload.setDeepLinkUrl(url.get());
        iOsPayload.setAppStoreUrl(CaixaPayloadValues.IOS_APP_STORE_URL);

        Desktop desktop = new Desktop();
        desktop.setUrl(url.get());

        payload.setAndroid(androidPayload);
        payload.setIos(iOsPayload);
        payload.setDesktop(desktop);

        return payload;
    }
}
