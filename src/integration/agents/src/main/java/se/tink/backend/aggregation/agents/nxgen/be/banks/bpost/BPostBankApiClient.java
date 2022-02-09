package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost;

import java.util.List;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request.BeginSessionRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request.LoginPINAuthRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request.LoginPINInitRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request.RegistrationChallengeResponseRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request.RegistrationExecuteRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request.RegistrationInitRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request.dto.LoginResponseDTO;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request.dto.RegistrationResponseDTO;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.product.account.AccountTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.product.account.BPostBankAccountsResponseDTO;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.product.account.BPostBankTransactionDTO;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.product.account.TransactionalAccountRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.entity.BPostBankAuthContext;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public class BPostBankApiClient {

    private TinkHttpClient httpClient;

    public BPostBankApiClient(TinkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String initSessionAndGetCSRFToken() throws AuthenticationException {
        try {
            return new BeginSessionRequest().call(httpClient);
        } catch (RequestException ex) {
            throw mapToAuthenticationException(ex, LoginError.NOT_SUPPORTED);
        }
    }

    public RegistrationResponseDTO registrationInit(BPostBankAuthContext authContext)
            throws AuthenticationException {
        try {
            return new RegistrationInitRequest(authContext).call(httpClient);
        } catch (RequestException ex) {
            throw mapToAuthenticationException(ex, LoginError.NOT_SUPPORTED);
        }
    }

    public RegistrationResponseDTO registrationAuthorize(
            BPostBankAuthContext authContext, String challengeResponse)
            throws AuthenticationException {
        try {
            String clearedSignCode = challengeResponse.replaceAll("\\s", "");
            return new RegistrationChallengeResponseRequest(authContext, clearedSignCode)
                    .call(httpClient);
        } catch (RequestException ex) {
            throw mapToAuthenticationException(ex, LoginError.INCORRECT_CHALLENGE_RESPONSE);
        }
    }

    public RegistrationResponseDTO registrationExecute(BPostBankAuthContext authContext)
            throws AuthenticationException {
        try {
            return new RegistrationExecuteRequest(authContext).call(httpClient);
        } catch (RequestException ex) {
            throw mapToAuthenticationException(ex, LoginError.NOT_SUPPORTED);
        }
    }

    public LoginResponseDTO loginPINInit(BPostBankAuthContext authContext)
            throws AuthenticationException {
        try {
            return new LoginPINInitRequest(authContext).call(httpClient);
        } catch (RequestException ex) {
            throw mapToAuthenticationException(ex, LoginError.NOT_SUPPORTED);
        }
    }

    public LoginResponseDTO loginPINAuth(BPostBankAuthContext authContext)
            throws AuthenticationException {
        try {
            return new LoginPINAuthRequest(authContext).call(httpClient);
        } catch (RequestException ex) {
            throw mapToAuthenticationException(ex, LoginError.NOT_SUPPORTED);
        }
    }

    public BPostBankAccountsResponseDTO fetchAccounts(BPostBankAuthContext authContext)
            throws RequestException {
        return new TransactionalAccountRequest(authContext).call(httpClient);
    }

    public List<BPostBankTransactionDTO> fetchAccountTransactions(
            TransactionalAccount account, int page, int pageSize, BPostBankAuthContext authContext)
            throws RequestException {
        int last = page * pageSize;
        int first = last - pageSize;
        return new AccountTransactionsRequest(authContext, first, last, account.getAccountNumber())
                .call(httpClient);
    }

    private AuthenticationException mapToAuthenticationException(
            RequestException ex, LoginError loginError) {
        return new LoginException(loginError, new LocalizableKey(ex.getMessage()));
    }
}
