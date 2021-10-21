package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AuthSessionStorageHelper;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.entities.LoginEntity;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.rpc.BankIdCollectResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.rpc.BankIdCompleteResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.rpc.BankIdInitRequest;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.rpc.BankIdInitResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities.SessionAccountPair;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;

public class AvanzaBankIdAuthenticator implements BankIdAuthenticator<BankIdInitResponse> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AvanzaBankIdAuthenticator.class);

    private final AvanzaApiClient apiClient;
    private final AuthSessionStorageHelper authSessionStorage;
    private final TemporaryStorage temporaryStorage;
    private final SessionStorage sessionStorage;

    public AvanzaBankIdAuthenticator(
            AvanzaApiClient apiClient,
            AuthSessionStorageHelper authSessionStorage,
            TemporaryStorage temporaryStorage,
            SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.authSessionStorage = authSessionStorage;
        this.temporaryStorage = temporaryStorage;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public BankIdInitResponse init(String ssn) throws BankIdException, AuthorizationException {
        final BankIdInitRequest request = new BankIdInitRequest(ssn);

        try {
            return apiClient.initBankId(request);
        } catch (HttpResponseException e) {

            handleInitBankIdErrors(e);

            throw e;
        }
    }

    @VisibleForTesting
    void handleInitBankIdErrors(HttpResponseException e) throws BankIdException {
        HttpResponse response = e.getResponse();

        if (response.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            throw BankIdError.ALREADY_IN_PROGRESS.exception(e);
        }

        if (response.getStatus() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(e);
        }
    }

    @Override
    public BankIdStatus collect(BankIdInitResponse reference)
            throws AuthenticationException, AuthorizationException {
        final String transactionId = reference.getTransactionId();
        // mask transaction ID from logging
        sessionStorage.put(StorageKeys.BANKID_TRANSACTION_ID, reference.getTransactionId());

        BankIdCollectResponse bankIdResponse;
        try {
            bankIdResponse = apiClient.collectBankId(transactionId);

            final BankIdStatus status = bankIdResponse.getBankIdStatus();
            if (status == BankIdStatus.DONE) {
                if (bankIdResponse.getLogins().isEmpty()) {
                    throw LoginError.NOT_CUSTOMER.exception();
                }
                // Complete the authentication and store auth session + security token for all
                // profiles
                bankIdResponse
                        .getLogins()
                        .forEach(loginEntity -> completeAuthentication(loginEntity, transactionId));
            }

            return status;
        } catch (HttpResponseException e) {
            handlePollBankIdErrors(e);
            throw e;
        }
    }

    @VisibleForTesting
    void handlePollBankIdErrors(HttpResponseException e) throws BankIdException {
        HttpResponse response = e.getResponse();

        if (response.getStatus() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(e);
        }

        if (response.hasBody()) {
            ErrorResponse errorResponse = response.getBody(ErrorResponse.class);

            if (errorResponse.isUserCancel()) {
                throw BankIdError.CANCELLED.exception(e);
            }

            if (errorResponse.isBankIdTimeout()) {
                throw BankIdError.TIMEOUT.exception(e);
            }

            LOGGER.error(
                    "Avanza BankID poll failed with error message: {}",
                    errorResponse.getMessage(),
                    e);
        }
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.empty();
    }

    @Override
    public Optional<OAuth2Token> getAccessToken() {
        return Optional.empty();
    }

    @Override
    public Optional<OAuth2Token> refreshAccessToken(String refreshToken) {
        return Optional.empty();
    }

    private void completeAuthentication(LoginEntity loginEntity, String transactionId) {

        BankIdCompleteResponse bankIdCompleteResponse =
                apiClient.completeBankId(transactionId, loginEntity.getCustomerId());

        putAuthCredentialsInAuthSessionStorage(bankIdCompleteResponse);

        List<SessionAccountPair> sessionAccountPairs =
                authSessionStorage.getAuthSessions().stream()
                        .flatMap(getSessionAccountPairs())
                        .collect(Collectors.toList());

        storeHolderNameIfAvailable(sessionAccountPairs);
    }

    @VisibleForTesting
    void storeHolderNameIfAvailable(List<SessionAccountPair> sessionAccountPairs) {
        try {
            storeHolderNameInTemporaryStorage(sessionAccountPairs);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_NOT_FOUND) {
                // If we're getting status code 404, that means that we can't fetch the holder name.
                return;
            }
            throw e;
        }
    }

    @VisibleForTesting
    Function<String, Stream<? extends SessionAccountPair>> getSessionAccountPairs() {
        return authSession ->
                apiClient.fetchAccounts(authSession).getAccounts().stream()
                        // The holdername is only available from a pension detail endpoint.
                        .filter(AccountEntity::isPensionAccount)
                        .map(AccountEntity::getAccountId)
                        .map(accountId -> new SessionAccountPair(authSession, accountId));
    }

    private void storeHolderNameInTemporaryStorage(List<SessionAccountPair> sessionAccountPairs) {
        for (SessionAccountPair sessionAccount : sessionAccountPairs) {
            String holderName =
                    apiClient
                            .fetchHolderNameFromPensionDetails(
                                    sessionAccount.getAccountId(), sessionAccount.getAuthSession())
                            .getInsuranceEntity()
                            .getName();
            if (!Strings.isNullOrEmpty(holderName)) {
                temporaryStorage.put(StorageKeys.HOLDER_NAME, holderName);
                return;
            }
        }
    }

    private void putAuthCredentialsInAuthSessionStorage(BankIdCompleteResponse response) {
        // This also masks this data from logs
        sessionStorage.put(
                String.format(StorageKeys.AUTH_SESSION_FORMAT, response.getAuthenticationSession()),
                response.getAuthenticationSession());
        sessionStorage.put(
                String.format(
                        StorageKeys.SECURITY_TOKEN_FORMAT, response.getAuthenticationSession()),
                response.getSecurityToken());
    }
}
