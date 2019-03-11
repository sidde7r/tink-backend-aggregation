package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities.EeILoginEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.EELoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.EELoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class EvoBancoAutoAuthenticator implements AutoAuthenticator {

    private final EvoBancoApiClient bankClient;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;
    private final Credentials credentials;

    public EvoBancoAutoAuthenticator(
            EvoBancoApiClient bankClient,
            Credentials credentials,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage) {
        this.bankClient = bankClient;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
    }

    @Override
    public void autoAuthenticate()
            throws SessionException, BankServiceException, AuthorizationException {
        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);

        try {
            // Construct login request from username and password
            bankClient.login(new LoginRequest(username, password));

            EeILoginEntity eeILoginEntity =
                    new EeILoginEntity.Builder()
                            .withNic(username)
                            .withOperatingSystem(EvoBancoConstants.HardCodedValues.OPERATING_SYSTEM)
                            .withPassword(password)
                            .withDeviceId(
                                    persistentStorage.get(EvoBancoConstants.Storage.DEVICE_ID))
                            .withAppId(EvoBancoConstants.HardCodedValues.APP_ID)
                            .withVersionApp(EvoBancoConstants.HardCodedValues.APP_VERSION)
                            .withMobileAccess(EvoBancoConstants.HardCodedValues.MOBILE_ACCESS)
                            .withApiVersion(EvoBancoConstants.HardCodedValues.API_VERSION)
                            .withEntityCode(
                                    sessionStorage.get(EvoBancoConstants.Storage.ENTITY_CODE))
                            .build();

            EELoginResponse eeLoginResponse =
                    bankClient.eeLogin(new EELoginRequest(eeILoginEntity));

            sessionStorage.put(
                    EvoBancoConstants.Storage.INTERNAL_ID_PE,
                    eeLoginResponse.getEeOLogin().getAnswer().getInternalIdPe());

        } catch (HttpResponseException e) {
            int statusCode = e.getResponse().getStatus();

            if (statusCode == EvoBancoConstants.StatusCodes.BAD_REQUEST_STATUS_CODE) {

                // We should be able to throw a Login Exception from autoAuthenticate method, this
                // is a workaround due to the fact that we don't support it at the moment
                try {
                    e.getResponse().getBody(EELoginResponse.class).handleReturnCode();
                } catch (LoginException e1) {
                    throw AuthorizationError.NO_VALID_PROFILE.exception(e1.getUserMessage());
                }
            } else {
                throw e;
            }
        } catch (LoginException e) {
            throw AuthorizationError.NO_VALID_PROFILE.exception(e.getUserMessage());
        }

        // Workaround needed due to the fact that EvoBanco's backend expects a check of the
        // global
        // position (accounts and cards)
        // immediately after the eeLogin, keep alive requests will fail if this is not done
        // first.
        bankClient.globalPositionFirstTime().handleReturnCode();
    }
}
