package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator;

import java.util.Objects;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.AktiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.RegistrationInitResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.RegistrationOtpResponse;
import se.tink.backend.aggregation.agents.utils.authentication.encap2.EncapClient;
import se.tink.backend.aggregation.agents.utils.authentication.encap2.enums.AuthenticationMethod;
import se.tink.backend.aggregation.agents.utils.authentication.encap2.models.DeviceAuthenticationResponse;
import se.tink.backend.aggregation.agents.utils.authentication.encap2.models.DeviceRegistrationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardInitValues;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class AktiaKeyCardAuthenticator implements KeyCardAuthenticator {
    private final AktiaApiClient apiClient;
    private final EncapClient encapClient;
    private final AktiaAuthenticationFlow aktiaAuthenticationFlow;

    private String username;
    private OAuth2Token registrationToken;

    public AktiaKeyCardAuthenticator(AktiaApiClient apiClient, EncapClient encapClient) {
        this.apiClient = apiClient;
        this.encapClient = encapClient;
        this.aktiaAuthenticationFlow = new AktiaAuthenticationFlow(apiClient, encapClient);
    }

    @Override
    public KeyCardInitValues init(String username, String password)
            throws AuthenticationException, AuthorizationException {

        // Note: `registrationInit()` throws exception if usr/pw is wrong.
        RegistrationInitResponse response = apiClient.registrationInit(username, password);

        this.username = username;
        this.registrationToken = response.getToken();
        return KeyCardInitValues.createFromCardIdAndCardIndex(
                response.getOtpCard(), response.getOtpIndex());
    }

    @Override
    public void authenticate(String code) throws AuthenticationException, AuthorizationException {
        RegistrationOtpResponse otpResponse =
                apiClient.registrationOtpChallengeResponse(registrationToken, code);
        if (!otpResponse.isSuccess() || Objects.isNull(otpResponse.getDeviceActivationCode())) {
            throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
        }

        String activationCode = otpResponse.getDeviceActivationCode();
        try {
            DeviceRegistrationResponse registrationResponse =
                    encapClient.registerDevice(username, activationCode);
            DeviceAuthenticationResponse authenticationResponse =
                    encapClient.authenticateDevice(AuthenticationMethod.DEVICE);

            if (!apiClient.registrationComplete(
                    registrationToken, authenticationResponse.getDeviceToken())) {
                // This should not happen.
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }

            // Authenticate after registration.
            aktiaAuthenticationFlow.authenticate();
        } finally {
            // Save device
            encapClient.saveDevice();
        }
    }
}
