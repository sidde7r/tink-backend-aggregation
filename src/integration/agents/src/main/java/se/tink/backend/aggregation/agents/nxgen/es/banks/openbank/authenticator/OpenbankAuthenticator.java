package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.authenticator;

import com.google.common.base.Strings;
import io.vavr.control.Try;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.OpenbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.OpenbankConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.OpenbankConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.i18n.LocalizableKey;
import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.OpenbankPredicates.HAS_INCORRECT_CREDENTIALS;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.OpenbankPredicates.HAS_INVALID_LOGIN_USERNAME_TYPE;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.OpenbankPredicates.IS_OPENBANK_ERROR_RESPONSE;

public class OpenbankAuthenticator implements Authenticator {
    private final OpenbankApiClient apiClient;
    private final SessionStorage sessionStorage;

    public OpenbankAuthenticator(OpenbankApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        final String username = credentials.getField(Field.Key.USERNAME);
        final String usernameType = credentials.getField(OpenbankConstants.USERNAME_TYPE);
        final String password = credentials.getField(Field.Key.PASSWORD);

        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        final LoginRequest request =
                new LoginRequest.Builder()
                        .withUsername(username)
                        .withUsernameType(usernameType)
                        .withPassword(password)
                        .withForce(1)
                        .build();

        Try.of(() -> apiClient.login(request))
                .filterTry(
                        LoginResponse::hasTokenCredential,
                        e -> LoginError.INCORRECT_CREDENTIALS.exception())
                .onSuccess(this::putAuthTokenInSessionStorage)
                .getOrElseThrow(this::handleExceptions);
    }

    private LoginException handleExceptions(Throwable e) {
        return Match(e).of(Case($(IS_OPENBANK_ERROR_RESPONSE), this::handleOpenbankErrors));
    }

    private LoginException handleOpenbankErrors(HttpResponseException hre) {
        return Match(hre.getResponse().getBody(ErrorResponse.class))
                .of(
                        Case(
                                $(HAS_INCORRECT_CREDENTIALS),
                                LoginError.INCORRECT_CREDENTIALS.exception()),
                        Case(
                                $(HAS_INVALID_LOGIN_USERNAME_TYPE),
                                LoginError.INCORRECT_CREDENTIALS.exception(
                                        LocalizableKey.of("Invalid username type"))));
    }

    private void putAuthTokenInSessionStorage(LoginResponse loginResponse) {
        loginResponse
                .getTokenCredential()
                .peek(authToken -> sessionStorage.put(Storage.AUTH_TOKEN, authToken));
    }
}
