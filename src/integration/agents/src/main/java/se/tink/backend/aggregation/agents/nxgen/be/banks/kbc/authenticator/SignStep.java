package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcConstants.Storage;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

final class SignStep implements AuthenticationStep {
    private final KbcAuthenticator kbcAuthenticator;
    private final SessionStorage sessionStorage;
    private final KbcApiClient apiClient;
    private final SupplementalInformationFormer supplementalInformationFormer;

    SignStep(
            KbcAuthenticator kbcAuthenticator,
            SessionStorage sessionStorage,
            KbcApiClient apiClient,
            SupplementalInformationFormer supplementalInformationFormer) {
        this.kbcAuthenticator = kbcAuthenticator;
        this.sessionStorage = sessionStorage;
        this.apiClient = apiClient;
        this.supplementalInformationFormer = supplementalInformationFormer;
    }

    @Override
    public AuthenticationResponse respond(final AuthenticationRequest request)
            throws LoginException, AuthorizationException {
        final Credentials credentials = request.getCredentials();
        final String panNr =
                kbcAuthenticator.verifyCredentialsNotNullOrEmpty(
                        credentials.getField(Field.Key.USERNAME));
        final byte[] cipherKey =
                EncodingUtils.decodeBase64String(
                        sessionStorage.get(KbcConstants.Encryption.AES_SESSION_KEY_KEY));
        final String responseCode =
                request.getUserInputs().stream()
                        .filter(input -> !input.contains(" "))
                        .findAny()
                        .orElseThrow(IllegalStateException::new);
        try {
            apiClient.registerLogon(panNr, responseCode, cipherKey);
        } catch (IllegalStateException e) {
            if (kbcAuthenticator.isIncorrectCardNumber(e) || kbcAuthenticator.isIncorrectCard(e)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception(
                        KbcConstants.UserMessage.INCORRECT_CARD_NUMBER.getKey());
            }

            if (kbcAuthenticator.isIncorrectLoginCodeLastAttempt(e)) {
                throw LoginError.INCORRECT_CREDENTIALS_LAST_ATTEMPT.exception();
            }

            if (kbcAuthenticator.isIncorrectLoginCode(e)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }

            throw e;
        }

        // registerLogon end

        final String signingId = apiClient.enrollDevice(cipherKey);

        sessionStorage.put(Storage.SIGNING_ID, signingId);

        final String signTypeId = apiClient.signTypeManual(signingId, cipherKey);
        final String signChallengeCode = apiClient.signChallenge(signTypeId, signingId, cipherKey);

        return new AuthenticationResponse(
                supplementalInformationFormer.formChallengeResponseFields(
                        Key.SIGN_CODE_DESCRIPTION, Key.SIGN_CODE_INPUT, signChallengeCode));
    }
}
