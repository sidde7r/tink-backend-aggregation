package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.SpankkiApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.SpankkiPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.SpankkiSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.authenticator.rpc.AddDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.authenticator.rpc.PinLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.authenticator.rpc.UsernamePasswordLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.rpc.SpankkiResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardInitValues;

public class SpankkiKeyCardAuthenticator implements KeyCardAuthenticator {

    private final SpankkiApiClient apiClient;
    private final SpankkiPersistentStorage persistentStorage;
    private final SpankkiSessionStorage sessionStorage;
    private final Credentials credentials;

    public SpankkiKeyCardAuthenticator(
            SpankkiApiClient apiClient,
            SpankkiPersistentStorage persistentStorage,
            SpankkiSessionStorage sessionStorage,
            Credentials credentials) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
        this.credentials = credentials;
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
        credentials.setSensitivePayload(Field.Key.OTP_INPUT, code);
        PinLoginResponse loginResponse = this.apiClient.loginPin(code);
        AddDeviceResponse addDeviceResponse = this.apiClient.addDevice();
        final String customerId = loginResponse.getCustomer().getCustomerId();
        credentials.setSensitivePayload("customer-id", customerId);
        sessionStorage.putCustomerId(customerId);
        sessionStorage.putCustomerEntity(loginResponse.getCustomer());
        persistentStorage.putDeviceId(addDeviceResponse.getDeviceId());
        persistentStorage.putDeviceToken(addDeviceResponse.getLoginToken());
    }
}
