package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.InitRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.InitResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpBankAuthenticateCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpBankAuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpBankLoginRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpBankLoginResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpBankMobileConfigurationsEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardInitValues;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.Field;

public class OpAuthenticator  implements KeyCardAuthenticator {

    public final OpBankApiClient apiClient;
    public final OpBankPersistentStorage persistentStorage;
    private String authToken;
    private Credentials credentials;

    public OpAuthenticator(OpBankApiClient client, OpBankPersistentStorage persistentStorage, Credentials credentials){
        this.apiClient = client;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
    }

    @Override
    public KeyCardInitValues init(String username, String password){
        InitResponseEntity iResponse =  apiClient.init(new InitRequestEntity());
        String authToken = OpAuthenticationTokenGenerator.calculateAuthToken(iResponse.getSeed());
        this.authToken = authToken;

        OpBankLoginRequestEntity request =
                new OpBankLoginRequestEntity()
                        .setUserid(username)
                        .setPassword(password)
                        .setApplicationInstanceId(persistentStorage.retrieveInstanceId());

        OpBankLoginResponseEntity loginResponse = apiClient.login(request);
        OpBankAuthenticateResponse aResponse = apiClient.authenticate();

        apiClient.adobeAnalyticsConfig(authToken, persistentStorage);

        credentials.setField(Field.Key.USERNAME, username);
        credentials.setField(Field.Key.PASSWORD, password);

        return new KeyCardInitValues(aResponse.getCardId(), aResponse.getUserKey());
    }

    @Override
    public void authenticate(String code) throws AuthenticationException, AuthorizationException {
        OpBankAuthenticateResponse response = apiClient
                .authenticate(new OpBankAuthenticateCodeRequest().setLang("en").setUserkey(code));
        apiClient.setRepresentationType();
        apiClient.postLogin(this.authToken, persistentStorage.retrieveInstanceId());
        updateApplicationInstanceId();
    }

    private void updateApplicationInstanceId() throws LoginException {
        String appInstanceId = persistentStorage.retrieveInstanceId();
        OpBankMobileConfigurationsEntity registerDevice = apiClient.enableExtendedMobileServices(appInstanceId);
        if(registerDevice.getStatus() != 0){
            throw LoginError.REGISTER_DEVICE_ERROR.exception();
        }

        persistentStorage.put(OpBankConstants.Authentication.APPLICATION_INSTANCE_ID, appInstanceId);
    }

}
