package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities.ErrorEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities.RootModel;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class CommerzbankPasswordAuthenticator implements PasswordAuthenticator {

    private CommerzbankApiClient apiClient;
    private SessionStorage sessionStorage;

    public CommerzbankPasswordAuthenticator(CommerzbankApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(String username, String password) throws AuthenticationException, AuthorizationException {

        //With cookies saved, the user can access all the other parts of the app
        HttpResponse response = null;
        try {
            response = apiClient.login(username, password);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("The credentials are not correct");
        }

        Optional<ErrorEntity> errorEntity = Optional.ofNullable(response.getBody(RootModel.class).getError());
        if (errorEntity.isPresent()) {
            if (response.getBody(RootModel.class).getError().getCode().equalsIgnoreCase(CommerzbankConstants.ERRORS.PIN_ERROR) ){
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            } else {
                throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception();
            }
        }
        String cookie = response.getCookies().toString();
        sessionStorage.put(CommerzbankConstants.HEADERS.COOKIE, cookie);
    }
}
