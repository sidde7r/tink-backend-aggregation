package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.authenticator;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.*;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.authenticator.rpc.AuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.authenticator.rpc.AuthenticateStatus;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

@Slf4j
@RequiredArgsConstructor
public class NordeaFIAuthenticator implements MultiFactorAuthenticator {

    private static final LocalizableKey CONFIRM_LOGIN =
            new LocalizableKey("Please open the Nordea Codes app and confirm login.");
    private static final int POLL_MAX_ATTEMPTS = 90;

    private final NordeaFIApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final SupplementalInformationController supplementalInformationController;
    private final Catalog catalog;
    private final Credentials credentials;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private String authReference;

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        init(credentials.getField(Field.Key.USERNAME));

        Future<AuthenticateResponse> authenticateResponseFuture = startPollingForResponse();

        askSupplementalInformationSync();

        stopPolling(authenticateResponseFuture);
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.THIRD_PARTY_APP;
    }

    private void init(String username) {
        try {
            apiClient.initCodesAuthentication(username);
        } catch (HttpResponseException e) {
            AuthenticateStatus response = e.getResponse().getBody(AuthenticateStatus.class);
            authReference = response.getReference();
            handleInitExceptions(response.getStatus());
        }

        if (authReference == null) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
    }

    private void handleInitExceptions(ThirdPartyAppStatus thirdPartyAppStatus) {
        if (thirdPartyAppStatus == ThirdPartyAppStatus.ALREADY_IN_PROGRESS) {
            throw ThirdPartyAppError.ALREADY_IN_PROGRESS.exception();
        } else if (thirdPartyAppStatus == ThirdPartyAppStatus.TIMED_OUT) {
            throw ThirdPartyAppError.TIMED_OUT.exception();
        } else if (thirdPartyAppStatus == ThirdPartyAppStatus.CANCELLED) {
            throw ThirdPartyAppError.CANCELLED.exception();
        } else if (thirdPartyAppStatus == ThirdPartyAppStatus.AUTHENTICATION_ERROR) {
            throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
        }
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

    private Future<AuthenticateResponse> startPollingForResponse() {
        return executor.submit(this::pollForResponse);
    }

    private void stopPolling(Future<?> future) throws LoginException {
        try {
            future.get();
            executor.shutdown();
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            log.error("[NordeaFI] Interrupted exception happened", e);
            throw new IllegalStateException("[NordeaFI] Interrupted exception happened", e);
        } catch (ExecutionException e) {
            throwKnownExceptionOrDefault(e);
        }
    }

    private AuthenticateResponse pollForResponse() {
        for (int i = 0; i < POLL_MAX_ATTEMPTS; i++) {
            AuthenticateResponse authenticationResponse = getAuthenticateResponse();
            if (authenticationResponse != null) {
                authenticationResponse.storeTokens(sessionStorage);
                return authenticationResponse;
            }
            Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
        }

        log.info("NordeaFIAuthenticator timed out internally.");
        throw ThirdPartyAppError.TIMED_OUT.exception();
    }

    private AuthenticateResponse getAuthenticateResponse() {
        try {
            return apiClient.getCodesAuthentication(
                    credentials.getField(Field.Key.USERNAME), authReference);

        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() != HttpStatus.SC_UNAUTHORIZED) {
                // ignore SC_UNAUTHORIZED, as it happens while waiting for approve
                log.error("[NordeaFI] HttpResponseException while polling for response", e);
                throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception(e);
            }
        }
        return null;
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
                    "[NordeaFI] Other error occurred while polling NordeaFI", e);
        }
    }
}
