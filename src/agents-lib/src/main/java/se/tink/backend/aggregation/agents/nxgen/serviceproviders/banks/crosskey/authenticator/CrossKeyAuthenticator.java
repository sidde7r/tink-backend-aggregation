package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator;

import se.tink.backend.aggregation.agents.banks.crosskey.utils.CrossKeyUtils;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.UnexpectedFailureException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc.AddDeviceRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc.AddDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc.ConfirmTanCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc.ConfirmTanCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc.LoginWithoutTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc.LoginWithoutTokenResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardInitValues;
import se.tink.backend.aggregation.rpc.Credentials;

public class CrossKeyAuthenticator implements KeyCardAuthenticator {

    private final CrossKeyApiClient apiClient;
    private final CrossKeyConfiguration agentConfiguration;
    private final CrossKeyPersistentStorage persistentStorage;
    private final Credentials credentials;

    public CrossKeyAuthenticator(CrossKeyApiClient apiClient, CrossKeyConfiguration agentConfiguration,
            CrossKeyPersistentStorage persistentStorage, Credentials credentials) {
        this.apiClient = apiClient;
        this.agentConfiguration = agentConfiguration;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
    }

    @Override
    public KeyCardInitValues init(String username, String password)
            throws AuthenticationException, AuthorizationException {

        apiClient.initSession();
        LoginWithoutTokenResponse challenge = apiClient.loginUsernamePassword(
                new LoginWithoutTokenRequest()
                        .setUsername(username)
                        .setPassword(password)
        );

        challenge.validate(() -> new UnexpectedFailureException(challenge, "Failure on login"));

        return new KeyCardInitValues(challenge.getTanPosition());
    }

    @Override
    public void authenticate(String code) throws AuthenticationException, AuthorizationException {
        ConfirmTanCodeResponse confirmation = apiClient.confirmTanCode(
                new ConfirmTanCodeRequest()
                        .setTan(code)
        );

        confirmation.validate(() -> new UnexpectedFailureException(confirmation, "Failure on confirming tan code"));

        AddDeviceResponse addDevice = apiClient.addDevice(
                new AddDeviceRequest()
                        .setUdId(CrossKeyUtils.generateUdIdFor(credentials.getUserId()))
        );

        addDevice.validate(() -> new UnexpectedFailureException(addDevice, "Failure on adding of new device"));

        persistentStorage.persist(addDevice);
    }
}
