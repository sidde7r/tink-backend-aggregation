package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator;

import org.apache.http.HttpStatus;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities.EeILoginEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.EELoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.EELoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.entities.ErrorEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
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
            bankClient.login(new LoginRequest(username, password));
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();
            handleErrorResponses(response);
            throw SessionError.SESSION_EXPIRED.exception(e);
        }

        try {

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
            if (e.getResponse().getStatus() == HttpStatus.SC_BAD_REQUEST) {
                // Let's just re-do linking(pinning)
                throw SessionError.SESSION_EXPIRED.exception(e);
            }
        }
        // Workaround needed due to the fact that EvoBanco's backend expects a check of the
        // global
        // position (accounts and cards)
        // immediately after the eeLogin, keep alive requests will fail if this is not done
        // first.
        bankClient.globalPositionFirstTime().handleReturnCode();
    }

    private void handleErrorResponses(HttpResponse response) {
        if (response.getStatus() == 403
                || response.getBody(ErrorEntity.class)
                        .getMessage()
                        .equalsIgnoreCase(ErrorMessages.AUTHENTICATION_ERROR_MSG)) {
            throw AuthorizationError.UNAUTHORIZED.exception();
        }
    }
}
