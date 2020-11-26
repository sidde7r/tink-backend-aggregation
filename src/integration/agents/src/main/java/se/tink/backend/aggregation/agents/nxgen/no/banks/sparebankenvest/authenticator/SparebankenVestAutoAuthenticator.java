package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.authenticator;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.SparebankenVestApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.SparebankenVestConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.authenticator.entities.SecurityParamsRequestBody;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.authenticator.utils.SparebankenVestAuthUtils;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapClient;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.enums.AuthenticationMethod;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.models.DeviceAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SparebankenVestAutoAuthenticator implements AutoAuthenticator {
    private final SparebankenVestApiClient apiClient;
    private final EncapClient encapClient;
    private final SessionStorage sessionStorage;

    private SparebankenVestAutoAuthenticator(
            SparebankenVestApiClient apiClient,
            EncapClient encapClient,
            SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.encapClient = encapClient;
        this.sessionStorage = sessionStorage;
    }

    public static SparebankenVestAutoAuthenticator create(
            SparebankenVestApiClient apiClient,
            EncapClient encapClient,
            SessionStorage sessionStorage) {
        return new SparebankenVestAutoAuthenticator(apiClient, encapClient, sessionStorage);
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        apiClient.initLogin();

        try {
            DeviceAuthenticationResponse deviceAuthenticationResponse =
                    encapClient.authenticateDevice(AuthenticationMethod.DEVICE_AND_PIN);

            sessionStorage.put(Storage.DEVICE_TOKEN, deviceAuthenticationResponse.getDeviceToken());
            sessionStorage.put(Storage.HARDWARE_ID, deviceAuthenticationResponse.getHardwareId());
            String htmlResponseString =
                    apiClient.authenticate(
                            deviceAuthenticationResponse.getDeviceToken(),
                            deviceAuthenticationResponse.getHardwareId());

            SecurityParamsRequestBody securityParamsRequestBody =
                    SparebankenVestAuthUtils.createSecurityParamsRequestBody(htmlResponseString);
            htmlResponseString =
                    apiClient.postSecurityParamsAuthentication(securityParamsRequestBody);

            securityParamsRequestBody =
                    SparebankenVestAuthUtils.createSecurityParamsRequestBody(htmlResponseString);
            apiClient.finalizeLogin(securityParamsRequestBody);
        } catch (IllegalStateException e) {
            throw SessionError.SESSION_EXPIRED.exception(e);
        } finally {
            encapClient.saveDevice();
        }
    }
}
