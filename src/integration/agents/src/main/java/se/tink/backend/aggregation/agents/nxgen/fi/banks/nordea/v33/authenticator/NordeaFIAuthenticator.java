package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.authenticator;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.authenticator.rpc.AuthenticateCode;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.authenticator.rpc.AuthenticateErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.authenticator.rpc.AuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.authenticator.rpc.AuthenticateTokenResponse;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.cryptography.hash.Hash;
import se.tink.libraries.encoding.EncodingUtils;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@Slf4j
public class NordeaFIAuthenticator implements MultiFactorAuthenticator {

    private static final LocalizableKey CONFIRM_LOGIN =
            new LocalizableKey("Please open the Nordea Codes app and confirm login.");
    private static final LocalizableKey AUTHENTICATION_COLLISION_ERROR_MESSAGE =
            new LocalizableKey(
                    "Several simultaneous identification/confirmation attempts. Open the authentication app to cancel the error.");
    private static final int POLL_MAX_ATTEMPTS = 90;

    private final NordeaFIApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final SupplementalInformationController supplementalInformationController;
    private final Catalog catalog;
    private final RandomValueGenerator randomValueGenerator;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private String sessionId;
    private String codeVerifier;

    public NordeaFIAuthenticator(
            NordeaFIApiClient apiClient,
            SessionStorage sessionStorage,
            SupplementalInformationController supplementalInformationController,
            Catalog catalog,
            RandomValueGenerator randomValueGenerator) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.supplementalInformationController = supplementalInformationController;
        this.catalog = catalog;
        this.randomValueGenerator = randomValueGenerator;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        init(credentials.getField(Field.Key.USERNAME));

        Future<AuthenticateTokenResponse> authenticateResponseFuture = startPollingForResponse();

        askSupplementalInformationSync();

        stopPolling(authenticateResponseFuture);
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    private AuthenticateResponse init(String username) {
        codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);
        try {
            AuthenticateResponse authenticateResponse =
                    apiClient.initAuthentication(username, codeChallenge);
            sessionId = authenticateResponse.getSessionId();
            return authenticateResponse;
        } catch (HttpResponseException e) {
            AuthenticateErrorResponse response =
                    e.getResponse().getBody(AuthenticateErrorResponse.class);
            handleAuthenticateExceptions(response.getStatus(), response.getRawError());
        }
        throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
    }

    private void askSupplementalInformationSync() {
        try {
            Field field = CommonFields.Instruction.build(catalog.getString(CONFIRM_LOGIN));
            supplementalInformationController.askSupplementalInformationSync(field);
        } catch (SupplementalInfoException e) {
            // ignore empty response!
            // we're actually not interested in response at all, we just show a text!
        }
    }

    private Future<AuthenticateTokenResponse> startPollingForResponse() {
        return executor.submit(this::pollForResponse);
    }

    private void stopPolling(Future<?> future) throws LoginException {
        try {
            future.get();
            executor.shutdown();
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            log.error(toErrorMessage("Interrupted exception happened"), e);
            throw new IllegalStateException(toErrorMessage("Interrupted exception happened"), e);
        } catch (ExecutionException e) {
            throwKnownExceptionOrDefault(e);
        }
    }

    private AuthenticateTokenResponse pollForResponse() {
        for (int i = 0; i < POLL_MAX_ATTEMPTS; i++) {
            AuthenticateResponse authenticationResponse = getAuthenticateStatus();
            if (authenticationResponse != null
                    && authenticationResponse.getStatus() != ThirdPartyAppStatus.WAITING) {
                return handleStatusResponse(authenticationResponse);
            }
            Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);
        }

        log.info(toErrorMessage("NordeaFIAuthenticator timed out internally."));
        apiClient.cancelAuthentication(sessionId);
        throw ThirdPartyAppError.TIMED_OUT.exception();
    }

    private AuthenticateTokenResponse handleStatusResponse(
            AuthenticateResponse authenticateResponse) {
        ThirdPartyAppStatus status = authenticateResponse.getStatus();
        if (status == ThirdPartyAppStatus.DONE) {
            return exchangeCodeForToken(authenticateResponse);
        } else {
            handleAuthenticateExceptions(status, authenticateResponse.getRawStatus());
        }
        throw new IllegalStateException(
                NordeaFIConstants.LogTags.NORDEA_FI_AUTHENTICATE
                        + " Unhandled status response for authentication");
    }

    private AuthenticateTokenResponse exchangeCodeForToken(
            AuthenticateResponse authenticateResponse) {
        String code = authenticateResponse.getCode();
        AuthenticateCode authenticateCode =
                apiClient.getAuthenticateCode(AuthenticateCode.builder().code(code).build());
        validateAuthenticateCode(authenticateCode);
        AuthenticateTokenResponse authenticateTokenResponse =
                apiClient.getAuthenticationToken(authenticateCode.getCode(), codeVerifier);
        authenticateTokenResponse.storeTokens(sessionStorage);
        return authenticateTokenResponse;
    }

    private AuthenticateResponse getAuthenticateStatus() {
        try {
            return apiClient.getAuthenticationStatus(sessionId);
        } catch (HttpResponseException e) {
            log.error(toErrorMessage("HttpResponseException while polling for response"), e);
            throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception(e);
        }
    }

    private void validateAuthenticateCode(AuthenticateCode authenticateCode) {
        if (authenticateCode == null || authenticateCode.getCode() == null) {
            throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
        }
    }

    private void handleAuthenticateExceptions(
            ThirdPartyAppStatus thirdPartyAppStatus, String rawStatus) {
        if (thirdPartyAppStatus == ThirdPartyAppStatus.ALREADY_IN_PROGRESS) {
            throw ThirdPartyAppError.ALREADY_IN_PROGRESS.exception(
                    AUTHENTICATION_COLLISION_ERROR_MESSAGE);
        } else if (thirdPartyAppStatus == ThirdPartyAppStatus.TIMED_OUT) {
            throw ThirdPartyAppError.TIMED_OUT.exception();
        } else if (thirdPartyAppStatus == ThirdPartyAppStatus.CANCELLED) {
            throw ThirdPartyAppError.CANCELLED.exception();
        } else if (thirdPartyAppStatus == ThirdPartyAppStatus.AUTHENTICATION_ERROR) {
            throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
        } else if (thirdPartyAppStatus == ThirdPartyAppStatus.UNKNOWN) {
            log.error(toErrorMessage("Unknown authenticate status response: {}"), rawStatus);
            throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
        }
    }

    private void throwKnownExceptionOrDefault(ExecutionException e) {
        Throwable cause = e.getCause();
        if (cause instanceof ThirdPartyAppException) {
            throw (ThirdPartyAppException) cause;
        } else if (cause instanceof BankServiceException) {
            throw (BankServiceException) cause;
        } else if (cause instanceof RuntimeException) {
            throw (RuntimeException) cause;
        } else {
            throw new IllegalStateException(
                    toErrorMessage("Other error occurred while polling NordeaFI"), e);
        }
    }

    private String generateCodeVerifier() {
        return randomValueGenerator.generateRandomBase64UrlEncoded(96);
    }

    private String generateCodeChallenge(String codeVerifier) {
        final byte[] digest = Hash.sha256(codeVerifier);
        return EncodingUtils.encodeAsBase64UrlSafe(digest);
    }

    private String toErrorMessage(String message) {
        return NordeaFIConstants.LogTags.NORDEA_FI_AUTHENTICATE + " " + message;
    }
}
