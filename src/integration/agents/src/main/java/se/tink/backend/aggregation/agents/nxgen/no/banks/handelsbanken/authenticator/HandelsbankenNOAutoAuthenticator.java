package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc.FirstLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc.FirstLoginResponse;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapClient;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.enums.AuthenticationMethod;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class HandelsbankenNOAutoAuthenticator implements AutoAuthenticator {
    private final HandelsbankenNOApiClient apiClient;
    private final EncapClient encapClient;
    private final SessionStorage sessionStorage;

    public HandelsbankenNOAutoAuthenticator(
            HandelsbankenNOApiClient apiClient,
            EncapClient encapClient,
            SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.encapClient = encapClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void autoAuthenticate() throws SessionException {
        apiClient.fetchAppInformation();
        try {
            String evryToken =
                    encapClient
                            .authenticateDevice(AuthenticationMethod.DEVICE_AND_PIN)
                            .getDeviceToken();
            sessionStorage.put(Storage.EVRY_TOKEN, evryToken);

            if (evryToken == null) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
            executeLogin(evryToken);
        } catch (IllegalStateException e) {
            throw SessionError.SESSION_EXPIRED.exception(e);
        } finally {
            encapClient.saveDevice();
        }
    }

    private void executeLogin(String evryToken) throws HttpResponseException {
        FirstLoginRequest firstLoginRequest = FirstLoginRequest.build(evryToken);
        FirstLoginResponse firstLoginResponse = apiClient.loginFirstStep(firstLoginRequest);

        sessionStorage.put(
                HandelsbankenNOConstants.Tags.ACCESS_TOKEN, firstLoginResponse.getAccessToken());

        apiClient.loginSecondStep();
    }
}
