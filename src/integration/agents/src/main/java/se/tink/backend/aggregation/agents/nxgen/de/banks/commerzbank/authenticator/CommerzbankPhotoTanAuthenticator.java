package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Strings;
import java.security.KeyPair;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
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
    private final long sleepTime;
    private final int retryAttempts;

    public CommerzbankPhotoTanAuthenticator(
            final PersistentStorage persistentStorage, final CommerzbankApiClient apiClient) {
        this(persistentStorage, apiClient, SLEEP_TIME, RETRY_ATTEMPTS);
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
            throw new IllegalStateException(
                    String.format(
                            "Excepted login status to be %s, but it was %s.",
                            Values.TAN_REQUESTED, loginInfoEntity.getLoginStatus()));
        }

        scaWithQrCode();

        pinDevice();
    }

    private void scaWithQrCode() throws LoginException {
        InitScaResponse initScaResponse = apiClient.initScaFlow();

        if (!initScaResponse.getInitScaEntity().isPushPhotoTanAvailable()) {
            throw LoginError.NOT_SUPPORTED.exception(
                    new LocalizableKey(
                            "We currently only support push PhotoTAN method, which is not available for your user."));
        }

        String processContextId = initScaResponse.getMetaData().getProcessContextId();

        apiClient.prepareScaApproval(processContextId);

        Retryer<ApprovalResponse> approvalStatusRetryer = getApprovalStatusRetryer();

        try {
            approvalStatusRetryer.call(() -> apiClient.approveSca(processContextId));
        } catch (ExecutionException | RetryException e) {
            log.warn("Authorization failed, approval status of SCA was not OK.", e);
            throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception(e);
        }

        apiClient.finaliseScaApproval(processContextId);
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
                error.getErrorMessage()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Login failed without error description present."));

        switch (errorMessage.getMessageId()) {
            case Error.PIN_ERROR:
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            case Error.ACCOUNT_SESSION_ACTIVE_ERROR:
                throw SessionError.SESSION_ALREADY_ACTIVE.exception();
            default:
                throw new IllegalStateException(
                        String.format(
                                "Login failed with unknown error message: %s",
                                errorMessage.getMessageId()));
        }
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }
}
