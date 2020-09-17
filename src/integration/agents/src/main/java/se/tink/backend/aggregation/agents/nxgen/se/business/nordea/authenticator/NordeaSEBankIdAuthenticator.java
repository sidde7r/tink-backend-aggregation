package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator;

import java.util.Optional;
import org.apache.commons.httpclient.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEConstants.NordeaBankIdStatus;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc.BankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc.ResultBankIdResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaSEBankIdAuthenticator implements BankIdAuthenticator<BankIdResponse> {
    private final NordeaSEApiClient apiClient;
    private final SessionStorage sessionStorage;
    public final String orgNumber;
    private static final Logger log = LoggerFactory.getLogger(NordeaSEBankIdAuthenticator.class);

    public NordeaSEBankIdAuthenticator(
            NordeaSEApiClient apiClient, SessionStorage sessionStorage, String orgNumber) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.orgNumber = orgNumber;
    }

    @Override
    public BankIdResponse init(String ssn)
            throws BankIdException, BankServiceException, AuthorizationException, LoginException {
        sessionStorage.put(StorageKeys.SSN, ssn);
        try {
            return apiClient.formInitBankIdLogin(ssn);
        } catch (HttpResponseException e) {
            return handleBankIdInitErrors(e);
        }
    }

    private BankIdResponse handleBankIdInitErrors(final HttpResponseException responseException)
            throws BankIdException, HttpResponseException, LoginException {
        if (isAlreadyInProgressException(responseException)) {
            throw BankIdError.ALREADY_IN_PROGRESS.exception(responseException);
        }
        if (isAuthenticationFailedException(responseException)) {
            throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(responseException);
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

    private boolean isAuthenticationFailedException(final HttpResponseException responseException) {
        BankIdResponse resp = responseException.getResponse().getBody(BankIdResponse.class);
        if (ErrorCodes.AUTHENTICATION_FAILED.equalsIgnoreCase(resp.getError())) {
            return true;
        }
        log.warn("Unhandled authentication error: {}", resp.getError());
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
            response.storeTokens(sessionStorage);
        } catch (HttpResponseException e) {
            final BankIdResponse resp = e.getResponse().getBody(BankIdResponse.class);
            if (NordeaBankIdStatus.AGREEMENTS_UNAVAILABLE.equalsIgnoreCase(resp.getError()))
                throw LoginError.NOT_CUSTOMER.exception(e);
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
    public Optional<OAuth2Token> getAccessToken() {
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
                throw BankIdError.INTERRUPTED.exception(e);
            }

            throw e;
        }
    }
}
