package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.authenticator.rpc.RsaKeyResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;

public final class BankiaAuthenticator implements PasswordAuthenticator {

    private final BankiaApiClient apiClient;

    public BankiaAuthenticator(BankiaApiClient apiClient) {
        super();
        this.apiClient = apiClient;
    }

    @Override
    public void authenticate(String username, String password) throws AuthenticationException {

        RsaKeyResponse loginKey = apiClient.getLoginKey();
        String rsaKey = loginKey.getRsaPublicKey();

        LoginResponse loginResponse =
                apiClient.login(
                        username, password, BankiaConstants.Default.EMPTY_EXECUTION_STRING, rsaKey);
        if (loginResponse.getJGidResponseCodError() != null
                && loginResponse
                        .getJGidResponseCodError()
                        .equalsIgnoreCase(BankiaConstants.Errors.WRONG_CREDENTIALS)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        apiClient.authorizeSession();
    }
}
