package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class EvoBancoPasswordAuthenticator implements PasswordAuthenticator {

    private final EvoBancoApiClient bankClient;
    private final SessionStorage sessionStorage;

    public EvoBancoPasswordAuthenticator(EvoBancoApiClient bankClient, SessionStorage sessionStorage) {
        this.bankClient = bankClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(String username, String password) throws AuthenticationException, AuthorizationException {

        // Construct login request from username and hashed password
        LoginResponse loginResponse = bankClient.login(new LoginRequest(username, password));

        //Needed for logout
        sessionStorage.put(EvoBancoConstants.QueryParams.AGREEMENT_BE, loginResponse.getUserinfo().getAgreementBE());
        sessionStorage.put(EvoBancoConstants.QueryParams.ENTITY_CODE, loginResponse.getUserinfo().getEntityCode());
        sessionStorage.put(EvoBancoConstants.QueryParams.USER_BE, loginResponse.getUserinfo().getUserBE());
        sessionStorage.put(EvoBancoConstants.Storage.USER_ID, loginResponse.getUserinfo().getMobilePhone());
        sessionStorage.put(EvoBancoConstants.Storage.ACCESS_TOKEN, OAuth2Token.createBearer(
                loginResponse.getAccessToken(), loginResponse.getRefreshToken(), loginResponse.getExpiresIn()));
    }
}
