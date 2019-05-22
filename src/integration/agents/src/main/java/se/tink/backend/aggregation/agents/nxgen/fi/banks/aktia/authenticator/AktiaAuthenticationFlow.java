package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.AktiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.AktiaConstants.InstanceStorage;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.entities.AuthenticationIdEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.AuthenticationInitResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.LoginDetailsResponse;
import se.tink.backend.aggregation.agents.utils.authentication.encap2.EncapClient;
import se.tink.backend.aggregation.agents.utils.authentication.encap2.enums.AuthenticationMethod;
import se.tink.backend.aggregation.agents.utils.authentication.encap2.models.DeviceAuthenticationResponse;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AktiaAuthenticationFlow {
    private final AktiaApiClient apiClient;
    private final EncapClient encapClient;
    private final Storage instanceStorage;

    public AktiaAuthenticationFlow(
            AktiaApiClient apiClient, EncapClient encapClient, final Storage instanceStorage) {
        this.apiClient = apiClient;
        this.encapClient = encapClient;
        this.instanceStorage = instanceStorage;
    }

    private String createAktiaAuthenticationId(String authId) {
        AuthenticationIdEntity entity = new AuthenticationIdEntity(authId);
        String entityString = SerializationUtils.serializeToString(entity);
        return EncodingUtils.encodeAsBase64String(entityString);
    }

    // Both registration (keyCard) and authentication (autoAuth) must login. Share the logic here.
    public void authenticate()
            throws SessionException, BankServiceException, AuthorizationException {
        try {
            DeviceAuthenticationResponse authenticationResponse =
                    encapClient.authenticateDevice(AuthenticationMethod.DEVICE);

            AuthenticationInitResponse authenticationInitResponse =
                    apiClient.authenticationInit(authenticationResponse.getDeviceToken());

            String authenticationId =
                    apiClient
                            .getAuthenticationId(authenticationInitResponse.getToken())
                            .orElseThrow(AuthorizationError.NO_VALID_PROFILE::exception);

            authenticationResponse =
                    encapClient.authenticateDevice(
                            AuthenticationMethod.DEVICE_AND_PIN,
                            createAktiaAuthenticationId(authenticationId));

            OAuth2Token token =
                    apiClient.getAndSaveAuthenticatedToken(authenticationResponse.getDeviceToken());
            if (!token.isValid()) {
                // This should not happen!
                throw new IllegalStateException("Expected to be logged in but token is invalid.");
            }

            // The login details must be queried before we can communicate with the API
            LoginDetailsResponse loginDetails = apiClient.getLoginDetails();
            instanceStorage.put(
                    InstanceStorage.USER_ACCOUNT_INFO, loginDetails.getUserAccountInfo());

        } finally {
            // Always save the device
            encapClient.saveDevice();
        }
    }
}
