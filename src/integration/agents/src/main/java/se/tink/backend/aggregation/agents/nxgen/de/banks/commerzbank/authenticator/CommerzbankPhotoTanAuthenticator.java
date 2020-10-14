package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Strings;
import java.security.KeyPair;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.Error;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.Values;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities.LoginInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.ApprovalResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.InitScaResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities.ErrorEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities.ErrorMessageEntity;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.serialization.utils.SerializationUtils;

@AllArgsConstructor
@Slf4j
public class CommerzbankPhotoTanAuthenticator implements TypedAuthenticator {

    private static final long SLEEP_TIME = 3_000L;
    private static final int RETRY_ATTEMPTS = 60;

    private final PersistentStorage persistentStorage;
    private final CommerzbankApiClient apiClient;
    private final SupplementalRequester supplementalRequester;
    private final long sleepTime;
    private final int retryAttempts;

    public CommerzbankPhotoTanAuthenticator(
            final PersistentStorage persistentStorage,
            final CommerzbankApiClient apiClient,
            final SupplementalRequester supplementalRequester) {
        this(persistentStorage, apiClient, supplementalRequester, SLEEP_TIME, RETRY_ATTEMPTS);
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);

        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        LoginResponse loginResponse = apiClient.manualLogin(username, password);

        if (loginResponse.getError() != null) {
            handleLoginError(loginResponse.getError());
        }

        LoginInfoEntity loginInfoEntity = loginResponse.getLoginInfoEntity();
        credentials.setSensitivePayload(
                CommerzbankConstants.LOGIN_INFO_ENTITY,
                SerializationUtils.serializeToString(loginInfoEntity));

        if (!loginInfoEntity.isTanRequestedStatus()) {
            if (Values.TAN_NOTACTIVE.equals(loginInfoEntity.getLoginStatus())) {
                throw LoginError.NO_AVAILABLE_SCA_METHODS.exception();
            }
            log.info("Unexpected login status: {}.", loginInfoEntity.getLoginStatus());
            throw LoginError.DEFAULT_MESSAGE.exception();
        }

        scaWithPhotoTan(credentials);

        pinDevice();
    }

    private void scaWithPhotoTan(Credentials credentials) throws LoginException {
        InitScaResponse initScaResponse = apiClient.initScaFlow();

        if (!initScaResponse.getInitScaEntity().isPushPhotoTanAvailable()) {
            throw LoginError.NOT_SUPPORTED.exception(
                    new LocalizableKey(
                            "We currently only support push PhotoTAN method, which is not available for your user."));
        }

        String processContextId = initScaResponse.getMetaData().getProcessContextId();

        apiClient.prepareScaApproval(processContextId);

        displayPrompt(credentials);

        approveSca(processContextId);

        apiClient.finaliseScaApproval(processContextId);
    }

    private void displayPrompt(Credentials credentials) {
        Field field =
                Field.builder()
                        .immutable(true)
                        .description("Please open PhotoTAN application and confirm the order")
                        .value("waiting for confirmation")
                        .name("name")
                        .build();

        credentials.setSupplementalInformation(
                SerializationUtils.serializeToString(Collections.singletonList(field)));
        credentials.setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);

        supplementalRequester.requestSupplementalInformation(credentials, false);
    }

    private void approveSca(String processContextId) throws LoginException {
        Retryer<ApprovalResponse> approvalStatusRetryer = getApprovalStatusRetryer();

        try {
            approvalStatusRetryer.call(() -> apiClient.approveSca(processContextId));
        } catch (ExecutionException | RetryException e) {
            log.warn("Authorization failed, approval status of SCA was not OK.", e);
            throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception(e);
        }
    }

    private Retryer<ApprovalResponse> getApprovalStatusRetryer() {
        return RetryerBuilder.<ApprovalResponse>newBuilder()
                .retryIfResult(
                        approvalResponse ->
                                approvalResponse == null
                                        || !approvalResponse.getStatusEntity().isApprovalOk())
                .withWaitStrategy(WaitStrategies.fixedWait(sleepTime, TimeUnit.MILLISECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(retryAttempts))
                .build();
    }

    /** Pin device and store appId and keypair which is necessary for the auto authentication. */
    private void pinDevice() {
        String appId = apiClient.initAppRegistration();
        persistentStorage.put(Storage.APP_ID, appId);

        apiClient.completeAppRegistration(appId);

        KeyPair keyPair = RSA.generateKeyPair(2048);
        persistentStorage.put(Storage.KEY_PAIR, SerializationUtils.serializeKeyPair(keyPair));

        apiClient.send2FactorToken(appId, keyPair.getPublic());
    }

    private void handleLoginError(ErrorEntity error) throws LoginException, SessionException {
        ErrorMessageEntity errorMessage =
                error.getErrorMessage().orElseThrow(LoginError.DEFAULT_MESSAGE::exception);

        switch (errorMessage.getMessageId()) {
            case Error.PIN_ERROR:
            case Error.VALIDATION_EXCEPTION:
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            case Error.ACCOUNT_SESSION_ACTIVE_ERROR:
                throw SessionError.SESSION_ALREADY_ACTIVE.exception();
            default:
                throw LoginError.DEFAULT_MESSAGE.exception(
                        String.format("Reason: %s", errorMessage.getMessageId()));
        }
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }
}
