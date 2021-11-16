package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator;

import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcDevice;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto.EnrollDeviceRoundTwoResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.encoding.EncodingUtils;

final class FinalStep implements AuthenticationStep {
    private KbcAuthenticator kbcAuthenticator;
    private final SessionStorage sessionStorage;
    private final KbcApiClient apiClient;

    FinalStep(
            KbcAuthenticator kbcAuthenticator,
            final SessionStorage sessionStorage,
            final KbcApiClient apiClient) {
        this.kbcAuthenticator = kbcAuthenticator;
        this.sessionStorage = sessionStorage;
        this.apiClient = apiClient;
    }

    public AuthenticationStepResponse execute(final AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        final String panNr =
                kbcAuthenticator.verifyCredentialsNotNullOrEmpty(
                        request.getCredentials().getField(Field.Key.USERNAME));
        final String signingId = sessionStorage.get(Storage.SIGNING_ID);
        final byte[] cipherKey =
                EncodingUtils.decodeBase64String(
                        sessionStorage.get(KbcConstants.Encryption.AES_SESSION_KEY_KEY));

        final String signResponseCode =
                request.getUserInputsAsList().stream()
                        .filter(input -> !input.contains(" "))
                        .findAny()
                        .orElseThrow(IllegalStateException::new);
        String finalSigningId =
                apiClient.signValidation(signResponseCode, panNr, signingId, cipherKey);
        apiClient.signValidation(signResponseCode, panNr, signingId, cipherKey);
        EnrollDeviceRoundTwoResponse enrollDeviceRoundTwoResponse =
                kbcAuthenticator.enrollDeviceRoundTwo(finalSigningId, cipherKey);

        KbcDevice device =
                kbcAuthenticator.createAndActivateKbcDevice(
                        enrollDeviceRoundTwoResponse, cipherKey);

        apiClient.logout(cipherKey);

        kbcAuthenticator.login(device);

        return AuthenticationStepResponse.executeNextStep();
    }
}
