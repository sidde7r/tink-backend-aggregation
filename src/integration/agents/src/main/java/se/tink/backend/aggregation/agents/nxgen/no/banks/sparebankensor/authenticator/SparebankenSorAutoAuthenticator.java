package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.rpc.FirstLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.rpc.FirstLoginResponse;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapClient;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.enums.AuthenticationMethod;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.models.DeviceAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SparebankenSorAutoAuthenticator implements AutoAuthenticator {
    private final SparebankenSorApiClient apiClient;
    private final EncapClient encapClient;
    private final SessionStorage sessionStorage;

    public SparebankenSorAutoAuthenticator(
            SparebankenSorApiClient apiClient,
            EncapClient encapClient,
            SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.encapClient = encapClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void autoAuthenticate() throws SessionException {
        apiClient.fetchAppInformation(); // only for getting a cookie, possible we must save this
        // cookie for later use in the first login request
        try {
            DeviceAuthenticationResponse deviceAuthenticationResponse =
                    encapClient.authenticateDevice(AuthenticationMethod.DEVICE_AND_PIN);
            String evryToken = deviceAuthenticationResponse.getDeviceToken();
            sessionStorage.put(Storage.EVRY_TOKEN, evryToken);
            executeLogin(evryToken);
        } catch (IllegalStateException e) {
            throw SessionError.SESSION_EXPIRED.exception(e);
        } finally {
            encapClient.saveDevice();
        }
    }

    private void executeLogin(String evryToken) {
        FirstLoginRequest firstLoginRequest = FirstLoginRequest.build(evryToken);
        FirstLoginResponse firstLoginResponse = apiClient.loginFirstStep(firstLoginRequest);

        sessionStorage.put(
                SparebankenSorConstants.Storage.ACCESS_TOKEN, firstLoginResponse.getAccessToken());
        // We might want to add some check on the second login response. Not doing it now since
        // because I don't know
        // what fields/values that signal an error. But if we get errors here we should add a check
        // for it.
        apiClient.loginSecondStep();
    }
}
