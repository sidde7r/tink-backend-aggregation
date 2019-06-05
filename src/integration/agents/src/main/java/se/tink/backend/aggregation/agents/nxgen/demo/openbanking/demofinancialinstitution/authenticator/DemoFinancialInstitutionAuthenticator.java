package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demofinancialinstitution.authenticator;

import com.google.common.base.Strings;
import io.vavr.control.Try;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demofinancialinstitution.DemoFinancialInstitutionApiClient;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demofinancialinstitution.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class DemoFinancialInstitutionAuthenticator implements PasswordAuthenticator {
    private DemoFinancialInstitutionApiClient apiClient;
    private SessionStorage sessionStorage;

    public DemoFinancialInstitutionAuthenticator(
            DemoFinancialInstitutionApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(final String username, final String password)
            throws AuthenticationException, AuthorizationException, LoginException {

        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        final LoginRequest loginRequest = new LoginRequest(username, password);

        Try.of(() -> apiClient.login(loginRequest))
                .onFailure(
                        HttpResponseException.class,
                        hre -> LoginError.INCORRECT_CREDENTIALS.exception());
    }
}
