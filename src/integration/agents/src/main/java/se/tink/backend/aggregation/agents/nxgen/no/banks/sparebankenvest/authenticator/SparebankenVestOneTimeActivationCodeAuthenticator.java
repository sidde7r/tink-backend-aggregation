package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.SparebankenVestApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.SparebankenVestConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.authenticator.entities.SecurityParamsRequestBody;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.authenticator.utils.SparebankenVestAuthUtils;
import se.tink.backend.aggregation.agents.utils.authentication.encap2.EncapClient;
import se.tink.backend.aggregation.agents.utils.authentication.encap2.models.DeviceRegistrationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SparebankenVestOneTimeActivationCodeAuthenticator implements PasswordAuthenticator {
    private final SparebankenVestApiClient apiClient;
    private final EncapClient encapClient;
    private final SessionStorage sessionStorage;

    private SparebankenVestOneTimeActivationCodeAuthenticator(
            SparebankenVestApiClient apiClient,
            EncapClient encapClient,
            SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.encapClient = encapClient;
        this.sessionStorage = sessionStorage;
    }

    public static SparebankenVestOneTimeActivationCodeAuthenticator create(
            SparebankenVestApiClient apiClient,
            EncapClient encapClient,
            SessionStorage sessionStorage) {
        return new SparebankenVestOneTimeActivationCodeAuthenticator(
                apiClient, encapClient, sessionStorage);
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        apiClient.initLogin();

        try {
            apiClient.preRegistration();

            DeviceRegistrationResponse deviceRegistrationResponse =
                    encapClient.registerDevice(username, password);
            sessionStorage.put(Storage.DEVICE_TOKEN, deviceRegistrationResponse.getDeviceToken());
            String htmlResponseString =
                    apiClient.activate(deviceRegistrationResponse.getDeviceToken());

            SecurityParamsRequestBody securityParamsRequestBody =
                    SparebankenVestAuthUtils.createSecurityParamsRequestBody(htmlResponseString);
            htmlResponseString = apiClient.postSecurityParamsActivation(securityParamsRequestBody);

            securityParamsRequestBody =
                    SparebankenVestAuthUtils.createSecurityParamsRequestBody(htmlResponseString);
            apiClient.finalizeLogin(securityParamsRequestBody);
        } finally {
            encapClient.saveDevice();
        }
    }
}
