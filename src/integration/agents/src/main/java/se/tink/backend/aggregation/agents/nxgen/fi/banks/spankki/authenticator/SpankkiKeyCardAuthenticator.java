package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.SpankkiApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.SpankkiPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.SpankkiSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.authenticator.rpc.AddDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.authenticator.rpc.PinLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.authenticator.rpc.UsernamePasswordLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.rpc.SpankkiResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardInitValues;

public class SpankkiKeyCardAuthenticator implements KeyCardAuthenticator {

    private final SpankkiApiClient apiClient;
    private final SpankkiPersistentStorage persistentStorage;
    private final SpankkiSessionStorage sessionStorage;

    public SpankkiKeyCardAuthenticator(
            SpankkiApiClient apiClient,
            SpankkiPersistentStorage persistentStorage,
            SpankkiSessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public KeyCardInitValues init(String username, String password)
            throws AuthenticationException, AuthorizationException {

        SpankkiResponse challengeResponse = this.apiClient.handleSetupChallenge();
        this.sessionStorage.putSessionId(challengeResponse.getSessionId());

        UsernamePasswordLoginResponse loginResponse =
                this.apiClient.loginUserPassword(username, password);

        return new KeyCardInitValues(loginResponse.getLoginStatus().getPinPosition());
    }

    @Override
    public void authenticate(String code) throws AuthenticationException, AuthorizationException {
        PinLoginResponse loginResponse = this.apiClient.loginPin(code);

        AddDeviceResponse addDeviceResponse = this.apiClient.addDevice();

        sessionStorage.putCustomerId(loginResponse.getCustomer().getCustomerId());
        sessionStorage.putCustomerEntity(loginResponse.getCustomer());

        persistentStorage.putDeviceId(addDeviceResponse.getDeviceId());
        persistentStorage.putDeviceToken(addDeviceResponse.getLoginToken());
    }
}
