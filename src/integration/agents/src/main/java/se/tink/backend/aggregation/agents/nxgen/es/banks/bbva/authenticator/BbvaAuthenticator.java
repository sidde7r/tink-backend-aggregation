package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator;

import io.vavr.control.Try;
import java.util.function.Consumer;
import java.util.function.Supplier;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaPredicates;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc.InitiateSessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.utils.BbvaUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
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

        Try.of(() -> apiClient.login(loginRequest).getBody(String.class))
                .map(String::toLowerCase)
                .filterTry(
                        BbvaPredicates.IS_LOGIN_WRONG_CREDENTIALS.negate(),
                        (Supplier<Throwable>) LoginError.INCORRECT_CREDENTIALS::exception)
                .filterTry(
                        BbvaPredicates.IS_LOGIN_SUCCESS,
                        () -> new IllegalStateException("Could not authenticate"))
                .transform(s -> apiClient.initiateSession())
                .onSuccess(putHolderNameInSessionStorage());
    }

    private Consumer<InitiateSessionResponse> putHolderNameInSessionStorage() {
        return response ->
                sessionStorage.put(BbvaConstants.StorageKeys.HOLDER_NAME, response.getName());
    }
}
