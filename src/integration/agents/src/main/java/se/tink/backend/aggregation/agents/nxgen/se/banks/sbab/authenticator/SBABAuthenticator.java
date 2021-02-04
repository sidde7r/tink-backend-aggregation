package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SBABApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SBABConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SBABConstants.HrefKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SBABConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.rpc.InitBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.rpc.PollBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc.StandardResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SBABAuthenticator implements BankIdAuthenticator<InitBankIdResponse> {
    private final SBABApiClient apiClient;
    private final SessionStorage sessionStorage;
    private String autoStartToken;

    public SBABAuthenticator(SBABApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public InitBankIdResponse init(String ssn)
            throws BankIdException, BankServiceException, AuthorizationException,
                    AuthenticationException {
        return refreshAutostartToken();
    }

    @Override
    public BankIdStatus collect(InitBankIdResponse reference)
            throws AuthenticationException, AuthorizationException {
        Uninterruptibles.sleepUninterruptibly(15, TimeUnit.SECONDS);
        try {
            final PollBankIdResponse pollBankIdResponse = apiClient.pollBankId(reference);
            final String accountsEndpoint =
                    apiClient.getEndpoint(pollBankIdResponse, HrefKeys.OVERVIEW);
            final String accessToken = pollBankIdResponse.getAccessTokenResponse().getAccessToken();
            sessionStorage.put(StorageKeys.ACCOUNTS_ENDPOINT, accountsEndpoint);
            sessionStorage.put(StorageKeys.ACCESS_TOKEN, accessToken);
            sessionStorage.put(StorageKeys.BEARER_TOKEN, "Bearer " + accessToken);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_BAD_REQUEST) {
                return BankIdStatus.WAITING;
            }
            if (e.getResponse().getStatus() == HttpStatus.SC_UNAUTHORIZED) {
                final String errorMessage =
                        e.getResponse().getBody(ErrorResponse.class).getMessage();

                if (ErrorMessages.NO_CLIENT.equalsIgnoreCase(errorMessage)) {
                    return BankIdStatus.NO_CLIENT;
                }
                if (ErrorMessages.CANCELLED.equalsIgnoreCase(errorMessage)) {
                    return BankIdStatus.CANCELLED;
                }
                return BankIdStatus.EXPIRED_AUTOSTART_TOKEN;
            }
            if (e.getResponse().getStatus() == HttpStatus.SC_CONFLICT) {
                throw LoginError.NOT_CUSTOMER.exception();
            }
            return BankIdStatus.FAILED_UNKNOWN;
        }
        return BankIdStatus.DONE;
    }

    @Override
    public InitBankIdResponse refreshAutostartToken()
            throws BankIdException, BankServiceException, AuthorizationException,
                    AuthenticationException {
        final StandardResponse standardResponse = apiClient.fetchAuthEndpoint();
        final InitBankIdResponse initBankIdResponse = apiClient.initBankId(standardResponse);
        autoStartToken = initBankIdResponse.getPendingAuthCodeResponse().getAutostartToken();

        return initBankIdResponse;
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.ofNullable(autoStartToken);
    }
}
