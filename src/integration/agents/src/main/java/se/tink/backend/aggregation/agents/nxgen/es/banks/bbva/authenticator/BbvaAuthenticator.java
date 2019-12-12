package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator;

import io.vavr.API;
import io.vavr.Predicates;
import io.vavr.control.Try;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaPredicates;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.utils.BbvaUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BbvaAuthenticator implements PasswordAuthenticator {
    private final SessionStorage sessionStorage;
    private BbvaApiClient apiClient;

    public BbvaAuthenticator(BbvaApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        final LoginRequest loginRequest =
                new LoginRequest(BbvaUtils.formatUsername(username), password);
        Try.of(() -> apiClient.login(loginRequest))
                .filterTry(
                        BbvaPredicates.IS_LOGIN_SUCCESS,
                        () -> new IllegalStateException("Could not authenticate"))
                .mapFailure(
                        API.Case(
                                API.$(Predicates.instanceOf(HttpResponseException.class)),
                                this::mapHttpErrors))
                .get();
    }

    private Exception mapHttpErrors(HttpResponseException e) {
        if (e.getResponse().getStatus() == HttpStatus.SC_FORBIDDEN) {
            return LoginError.INCORRECT_CREDENTIALS.exception();
        }
        return e;
    }
}
