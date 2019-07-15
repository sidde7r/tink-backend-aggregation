package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator;

import java.util.Optional;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaAuthSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.entities.LoginEntity;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.rpc.BankIdCollectResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.rpc.BankIdCompleteResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.rpc.BankIdInitRequest;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.rpc.BankIdInitResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;

public class AvanzaBankIdAuthenticator implements BankIdAuthenticator<BankIdInitResponse> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AvanzaBankIdAuthenticator.class);

    private final AvanzaApiClient apiClient;
    private final AvanzaAuthSessionStorage authSessionStorage;
    private final TemporaryStorage temporaryStorage;

    public AvanzaBankIdAuthenticator(
            AvanzaApiClient apiClient,
            AvanzaAuthSessionStorage authSessionStorage,
            TemporaryStorage temporaryStorage) {
        this.apiClient = apiClient;
        this.authSessionStorage = authSessionStorage;
        this.temporaryStorage = temporaryStorage;
    }

    @Override
    public BankIdInitResponse init(String ssn) throws BankIdException, AuthorizationException {
        final BankIdInitRequest request = new BankIdInitRequest(ssn);

        try {
            return apiClient.initBankId(request);
        } catch (HttpResponseException e) {

            handleInitBankIdErrors(e.getResponse());

            throw e;
        }
    }

    private void handleInitBankIdErrors(HttpResponse response) throws BankIdException {

        if (response.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            throw BankIdError.ALREADY_IN_PROGRESS.exception();
        }

        if (response.getStatus() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
    }

    @Override
    public BankIdStatus collect(BankIdInitResponse reference)
            throws AuthenticationException, AuthorizationException {
        final String transactionId = reference.getTransactionId();

        BankIdCollectResponse bankIdResponse;
        try {
            bankIdResponse = apiClient.collectBankId(transactionId);
        } catch (HttpResponseException e) {

            handlePollBankIdErrors(e.getResponse());

            throw e;
        }

        final BankIdStatus status = bankIdResponse.getBankIdStatus();

        if (status == BankIdStatus.DONE) {
            // Complete the authentication and store auth session + security token for all profiles
            bankIdResponse
                    .getLogins()
                    .forEach(loginEntity -> completeAuthentication(loginEntity, transactionId));

            temporaryStorage.put(StorageKeys.HOLDER_NAME, bankIdResponse.getName());
        }

        return status;
    }

    private void handlePollBankIdErrors(HttpResponse response) throws BankIdException {

        if (response.getStatus() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }

        if (response.hasBody()) {
            ErrorResponse errorResponse = response.getBody(ErrorResponse.class);

            if (errorResponse.isUserCancel()) {
                throw BankIdError.CANCELLED.exception();
            }

            if (errorResponse.isBankIdTimeout()) {
                throw BankIdError.TIMEOUT.exception();
            }

            LOGGER.error(
                    "Avanza BankID poll failed with error message: {}", errorResponse.getMessage());
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
    }

    private void putAuthCredentialsInAuthSessionStorage(BankIdCompleteResponse response) {
        authSessionStorage.put(response.getAuthenticationSession(), response.getSecurityToken());
    }
}
