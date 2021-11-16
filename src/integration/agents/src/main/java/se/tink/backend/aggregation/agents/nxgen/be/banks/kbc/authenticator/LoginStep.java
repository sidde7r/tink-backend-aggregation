package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.encoding.EncodingUtils;

final class LoginStep implements AuthenticationStep {

    private KbcAuthenticator kbcAuthenticator;
    private final SessionStorage sessionStorage;
    private final KbcApiClient apiClient;
    private final SupplementalInformationFormer supplementalInformationFormer;

    LoginStep(
            KbcAuthenticator kbcAuthenticator,
            final SessionStorage sessionStorage,
            final KbcApiClient apiClient,
            final SupplementalInformationFormer supplementalInformationFormer) {
        this.kbcAuthenticator = kbcAuthenticator;
        this.sessionStorage = sessionStorage;
        this.apiClient = apiClient;
        this.supplementalInformationFormer = supplementalInformationFormer;
    }

    @Override
    public AuthenticationStepResponse execute(final AuthenticationRequest request)
            throws LoginException, AuthorizationException {
        final Credentials credentials = request.getCredentials();

        kbcAuthenticator.verifyCredentialsNotNullOrEmpty(credentials.getField(Field.Key.USERNAME));

        final byte[] cipherKey = KbcAuthenticator.generateCipherKey();
        sessionStorage.put(
                KbcConstants.Encryption.AES_SESSION_KEY_KEY,
                EncodingUtils.encodeAsBase64String(cipherKey));
        apiClient.prepareSession(cipherKey);

        String challengeCode = apiClient.challenge(cipherKey);

        return AuthenticationStepResponse.requestForSupplementInformation(
                new SupplementInformationRequester.Builder()
                        .withFields(
                                supplementalInformationFormer.formChallengeResponseFields(
                                        Key.LOGIN_DESCRIPTION, Key.LOGIN_INPUT, challengeCode))
                        .build());
    }
}
