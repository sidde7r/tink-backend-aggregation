package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants.AuthenticationParams;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants.TemporaryStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.entities.Pin1ScaEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.entities.SmsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.ScaResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.agents.utils.crypto.LaCaixaPasswordHash;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AutomaticAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.OtpStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.UsernamePasswordAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.i18n.Catalog;

public class LaCaixaMultifactorAuthenticator extends StatelessProgressiveAuthenticator {
    private final LaCaixaApiClient apiClient;
    private final CredentialsRequest credentialsRequest;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final Storage authStorage;
    private final LogMasker logMasker;
    private final List<? extends AuthenticationStep> authenticationSteps;
    private final AuthenticationStep otpStep;
    private final AuthenticationStep codeCardStep;
    private final AuthenticationStep appStep;
    private final AuthenticationStep finalizeStep;

    public LaCaixaMultifactorAuthenticator(
            Catalog catalog,
            LaCaixaApiClient apiClient,
            CredentialsRequest credentialsRequest,
            SupplementalInformationFormer supplementalInformationFormer,
            SupplementalInformationHelper supplementalInformationHelper,
            Storage authStorage,
            LogMasker logMasker) {
        this.apiClient = apiClient;
        this.credentialsRequest = credentialsRequest;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.authStorage = authStorage;
        this.logMasker = logMasker;

        this.otpStep = new OtpStep(this::processOtp, supplementalInformationFormer);
        this.codeCardStep = new CodeCardStep(catalog, authStorage);
        this.appStep = new AutomaticAuthenticationStep(this::waitForAppSign, "caixabankSign");
        this.finalizeStep =
                new AutomaticAuthenticationStep(this::finalizeEnrolment, "finalizeEnrolment");
        authenticationSteps =
                ImmutableList.of(
                        new UsernamePasswordAuthenticationStep(this::login),
                        new AutomaticAuthenticationStep(
                                this::initiateEnrolment, "initiateEnrolment"),
                        appStep,
                        codeCardStep,
                        finalizeStep,
                        otpStep,
                        finalizeStep);
    }

    @Override
    public List<? extends AuthenticationStep> authenticationSteps() {
        return authenticationSteps;
    }

    @Override
    public boolean isManualAuthentication(CredentialsRequest request) {
        return request.isCreate();
    }

    private void login(String username, String password) throws LoginException {
        // Requests a session ID from the server in the form of a cookie.
        // Also gets seed for password hashing.
        SessionResponse sessionResponse = apiClient.initializeSession();

        final String pin =
                LaCaixaPasswordHash.hash(
                        sessionResponse.getSeed(), sessionResponse.getIterations(), password);
        logMasker.addNewSensitiveValuesToMasker(Collections.singleton(pin));

        // Construct login request from username and hashed password
        apiClient.login(new LoginRequest(username, pin));
    }

    private AuthenticationStepResponse initiateEnrolment() {
        // only ask for SCA on manual authentication
        if (!isManualAuthentication(credentialsRequest)) {
            return AuthenticationStepResponse.authenticationSucceeded();
        }

        // ask for SCA
        final ScaResponse response = apiClient.initiateEnrolment();
        return handleScaResponse(response);
    }

    private AuthenticationStepResponse handleScaResponse(ScaResponse response) {
        switch (response.getScaType().toUpperCase()) {
            case AuthenticationParams.SCA_TYPE_APP:
                // SCA with CaixaBank Sign app
                return AuthenticationStepResponse.executeStepWithId(appStep.getIdentifier());
            case AuthenticationParams.SCA_TYPE_PASSWORD:
                // Password again
                return handlePasswordSca(response.getPin1Sca());
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

    private AuthenticationStepResponse handlePasswordSca(Pin1ScaEntity pin1Sca) {
        final String password = credentialsRequest.getCredentials().getField(Key.PASSWORD);
        final String code =
                LaCaixaPasswordHash.hash(pin1Sca.getSeed(), pin1Sca.getIterations(), password);
        logMasker.addNewSensitiveValuesToMasker(Collections.singleton(code));
        final ScaResponse response = apiClient.authorizeSCA(code);
        return handleScaResponse(response);
    }

    private void processOtp(String otp, Credentials credentials) {
        final SmsEntity smsData =
                authStorage
                        .get(TemporaryStorage.SCA_SMS, SmsEntity.class)
                        .orElseThrow(() -> new IllegalStateException("SCA SMS Entity not found"));
        final String code =
                LaCaixaPasswordHash.hash(smsData.getSeed(), smsData.getIterations(), otp);
        authStorage.put(TemporaryStorage.ENROLMENT_CODE, code);
    }

    private AuthenticationStepResponse finalizeEnrolment() {
        final String code = Strings.nullToEmpty(authStorage.get(TemporaryStorage.ENROLMENT_CODE));
        logMasker.addNewSensitiveValuesToMasker(Collections.singleton(code));
        apiClient.finalizeEnrolment(code);
        return AuthenticationStepResponse.authenticationSucceeded();
    }

    private AuthenticationStepResponse waitForAppSign() {
        apiClient.initiateAuthSignature();

        // launch signing app
        supplementalInformationHelper.openThirdPartyApp(
                ThirdPartyAppAuthenticationPayload.of(new URL(AuthenticationParams.SIGNING_URL)));

        // poll
        final Retryer<Boolean> signPoller =
                RetryerBuilder.<Boolean>newBuilder()
                        .retryIfResult(result -> !result.booleanValue())
                        .withWaitStrategy(WaitStrategies.fixedWait(2, TimeUnit.SECONDS))
                        .withStopStrategy(StopStrategies.stopAfterAttempt(90))
                        .build();
        try {
            signPoller.call(() -> apiClient.verifyAuthSignature().isOperationSigned());
        } catch (ExecutionException | RetryException e) {
            e.printStackTrace();
        }

        authStorage.put(TemporaryStorage.ENROLMENT_CODE, "");
        return AuthenticationStepResponse.executeStepWithId(finalizeStep.getIdentifier());
    }
}
