package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities.EeILoginEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.EELoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.EELoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.rpc.GlobalPositionFirstTimeResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class EvoBancoPasswordAuthenticator implements PasswordAuthenticator {

    private final EvoBancoApiClient bankClient;
    private final SessionStorage sessionStorage;

    public EvoBancoPasswordAuthenticator(
            EvoBancoApiClient bankClient, SessionStorage sessionStorage) {
        this.bankClient = bankClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {

        // Construct login request from username and hashed password
        LoginResponse loginResponse = bankClient.login(new LoginRequest(username, password));

        // Needed for other requests
        sessionStorage.put(
                EvoBancoConstants.Storage.AGREEMENT_BE,
                loginResponse.getUserinfo().getAgreementBE());
        sessionStorage.put(
                EvoBancoConstants.Storage.ENTITY_CODE, loginResponse.getUserinfo().getEntityCode());
        sessionStorage.put(
                EvoBancoConstants.Storage.USER_BE, loginResponse.getUserinfo().getUserBE());
        sessionStorage.put(
                EvoBancoConstants.Storage.USER_ID, loginResponse.getUserinfo().getMobilePhone());
        sessionStorage.put(
                EvoBancoConstants.Storage.ACCESS_TOKEN,
                OAuth2Token.createBearer(
                        loginResponse.getAccessToken(),
                        loginResponse.getRefreshToken(),
                        loginResponse.getExpiresIn()));

        EeILoginEntity eeILoginEntity =
                new EeILoginEntity.Builder()
                        .withNic(username)
                        .withOperatingSystem(EvoBancoConstants.RequestValues.OPERATING_SYSTEM)
                        .withPassword(password)
                        .withDeviceId(EvoBancoConstants.RequestValues.DEVICE_ID)
                        .withAppId(EvoBancoConstants.RequestValues.APP_ID)
                        .withVersionApp(EvoBancoConstants.RequestValues.APP_VERSION)
                        .withMobileAccess(EvoBancoConstants.RequestValues.MOBILE_ACCESS)
                        .withApiVersion(EvoBancoConstants.RequestValues.API_VERSION)
                        .withEntityCode(sessionStorage.get(EvoBancoConstants.Storage.ENTITY_CODE))
                        .build();
        EELoginResponse eeLoginResponse = bankClient.eeLogin(new EELoginRequest(eeILoginEntity));

        sessionStorage.put(
                EvoBancoConstants.Storage.INTERNAL_ID_PE,
                eeLoginResponse.getEeOLogin().getAnswer().getInternalIdPe());

        // Workaround needed due to the fact that EvoBanco's backend expects a check of the global
        // position (accounnts and cards)
        // immediately after the eeLogin, keep alive requests will fail if this is not done first,
        // also this can only be done once
        // i.e. when refreshing accounts info we will just retrieve the info that we got here that
        // is stored in session storage.
        GlobalPositionFirstTimeResponse globalPositionFirstTimeResponse =
                bankClient.globalPositionFirstTime();
    }
}
