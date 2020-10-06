package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator;

import com.google.api.client.http.HttpStatusCodes;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.InitResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpBankAuthenticateCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpBankAuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpBankLoginRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpBankLoginResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpBankMobileConfigurationsEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardInitValues;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class OpKeyCardAuthenticator implements KeyCardAuthenticator {
    public final OpBankApiClient apiClient;
    public final OpBankPersistentStorage persistentStorage;
    private String authToken;
    private Credentials credentials;
    private SessionStorage sessionStorage;

    public OpKeyCardAuthenticator(
            OpBankApiClient client,
            OpBankPersistentStorage persistentStorage,
            Credentials credentials,
            SessionStorage sessionStorage) {
        this.apiClient = client;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public KeyCardInitValues init(String username, String password) throws LoginException {
        credentials.setSensitivePayload(Field.Key.USERNAME, username);
        credentials.setSensitivePayload(Field.Key.PASSWORD, password);
        InitResponseEntity iResponse = apiClient.init();
        iResponse.validateResponse();
        authToken = OpAuthenticationTokenGenerator.calculateAuthToken(iResponse.getSeed());
        credentials.setSensitivePayload(Field.Key.ACCESS_TOKEN, authToken);

        OpBankLoginRequestEntity request =
                new OpBankLoginRequestEntity()
                        .setUserid(username)
                        .setPassword(password)
                        .setApplicationInstanceId(persistentStorage.retrieveInstanceId());

        try {
            final OpBankLoginResponseEntity loginResponse = apiClient.login(request);
            sessionStorage.put(OpBankConstants.Storage.FULL_NAME, loginResponse.getName());
        } catch (HttpResponseException e) {
            handleAuthenticationException(e);
        }

        OpBankAuthenticateResponse aResponse = apiClient.authenticate();
        apiClient.instance(authToken);
        apiClient.auth(authToken);
        apiClient.adobeAnalyticsConfig(authToken, persistentStorage);

        credentials.setField(Field.Key.USERNAME, username);
        credentials.setField(Field.Key.PASSWORD, password);

        return new KeyCardInitValues(aResponse.getUserKey());
    }

    @Override
    public void authenticate(String code) throws AuthenticationException, AuthorizationException {
        credentials.setSensitivePayload(Field.Key.OTP_INPUT, code);

        try {
            apiClient.authenticate(
                    new OpBankAuthenticateCodeRequest().setLang("en").setUserkey(code));
        } catch (HttpResponseException e) {
            handleAuthenticationException(e);
        }

        apiClient.setRepresentationType();
        apiClient.postLogin(authToken, persistentStorage.retrieveInstanceId());

        // update application instance id will throw if we are not allowed
        // to pin the device
        registerDevice(persistentStorage.retrieveInstanceId());
    }

    private void handleAuthenticationException(HttpResponseException e) throws LoginException {
        HttpResponse response = e.getResponse();
        if (response.getStatus() == HttpStatusCodes.STATUS_CODE_UNAUTHORIZED) {
            ErrorResponse errorResponse = response.getBody(ErrorResponse.class);

            if (errorResponse.isIncorrectLoginCredentials()) {
                throw LoginError.INCORRECT_CREDENTIALS.exception(e);
            }
        }

        throw e;
    }

    private void registerDevice(String appInstanceId) throws LoginException {
        OpBankMobileConfigurationsEntity registerDevice =
                apiClient.enableExtendedMobileServices(appInstanceId);
        if (registerDevice.getStatus() != 0) {
            throw LoginError.REGISTER_DEVICE_ERROR.exception();
        }

        persistentStorage.put(
                OpBankConstants.Authentication.APPLICATION_INSTANCE_ID, appInstanceId);
    }
}
