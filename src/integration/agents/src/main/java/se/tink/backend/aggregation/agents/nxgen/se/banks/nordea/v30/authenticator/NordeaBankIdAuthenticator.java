package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.authenticator;

import java.util.Optional;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants.NordeaBankIdStatus;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.authenticator.rpc.BankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.authenticator.rpc.ResultBankIdResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaBankIdAuthenticator implements BankIdAuthenticator<BankIdResponse> {
    private final NordeaSEApiClient apiClient;
    private final SessionStorage sessionStorage;
    private static final Logger log = LoggerFactory.getLogger(NordeaBankIdAuthenticator.class);

    public NordeaBankIdAuthenticator(NordeaSEApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public BankIdResponse init(String ssn)
            throws BankIdException, BankServiceException, AuthorizationException {
        sessionStorage.put(StorageKeys.SSN, ssn);
        try {
            return apiClient.formInitBankIdLogin(ssn);
        } catch (HttpResponseException e) {
            return handleBankIdInitErrors(e);
        }
    }

    private BankIdResponse handleBankIdInitErrors(final HttpResponseException responseException)
            throws BankIdException, HttpResponseException {
        if (isAlreadyInProgressException(responseException)) {
            throw BankIdError.ALREADY_IN_PROGRESS.exception();
        }
        if (shouldKeepPolling(responseException)) {
            return responseException.getResponse().getBody(BankIdResponse.class);
        }
        throw responseException;
    }

    private boolean isAlreadyInProgressException(final HttpResponseException responseException) {
        BankIdResponse resp = responseException.getResponse().getBody(BankIdResponse.class);
        if (responseException.getResponse().getStatus() == HttpStatus.SC_CONFLICT) {
            return true;
        }
        if (resp.getError().equalsIgnoreCase(ErrorCodes.AUTHENTICATION_COLLISION)) {
            return true;
        }
        log.warn("Unhandled BankID error: {}", resp.getError());
        return false;
    }

    private boolean shouldKeepPolling(final HttpResponseException responseException) {
        if (responseException.getResponse().getStatus() != HttpStatus.SC_UNAUTHORIZED) {
            return false;
        }
        BankIdResponse resp = responseException.getResponse().getBody(BankIdResponse.class);
        return BankIdStatus.WAITING.equals(resp.getBankIdStatus());
    }

    @Override
    public BankIdStatus collect(BankIdResponse reference)
            throws AuthenticationException, AuthorizationException {
        try {
            ResultBankIdResponse response = getPollResponse(reference);
            sessionStorage.put(StorageKeys.ACCESS_TOKEN, response.getAccessToken());
            sessionStorage.put(StorageKeys.REFRESH_TOKEN, response.getRefreshToken());
            sessionStorage.put(StorageKeys.TOKEN_TYPE, response.getTokenType());
        } catch (HttpResponseException e) {
            final BankIdResponse resp = e.getResponse().getBody(BankIdResponse.class);
            if (NordeaBankIdStatus.AGREEMENTS_UNAVAILABLE.equalsIgnoreCase(resp.getError())) {
                throw LoginError.NOT_CUSTOMER.exception();
            }
            return e.getResponse().getBody(BankIdResponse.class).getBankIdStatus();
        }
        // If request does not generate a http error we have successfully authenticated.
        return BankIdStatus.DONE;
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.empty();
    }

    @Override
    public Optional<OAuth2Token> getAcessToken() {
        return Optional.empty();
    }

    @Override
    public Optional<OAuth2Token> refreshAccessToken(String refreshToken) {
        return Optional.empty();
    }

    private ResultBankIdResponse getPollResponse(BankIdResponse reference) throws BankIdException {
        try {
            return apiClient.formPollBankIdLogin(reference, sessionStorage.get(StorageKeys.SSN));
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_CONFLICT) {
                throw BankIdError.INTERRUPTED.exception();
            }

            throw e;
        }
    }
}
