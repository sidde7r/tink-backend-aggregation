package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator;

import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.GenerateChallengeResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;

final class LoginStep implements AuthenticationStep {

    private final AxaApiClient apiClient;
    private final AxaStorage storage;
    private final SupplementalInformationFormer supplementalInformationFormer;

    LoginStep(
            final AxaApiClient apiClient,
            final AxaStorage storage,
            final SupplementalInformationFormer supplementalInformationFormer) {
        this.apiClient = apiClient;
        this.storage = storage;
        this.supplementalInformationFormer = supplementalInformationFormer;
    }

    @Override
    public AuthenticationResponse respond(final AuthenticationRequest request) {
        final String basicAuth = AxaConstants.Request.BASIC_AUTH;
        final String ucrid = generateUcrid();
        final GenerateChallengeResponse challengeResponse =
                apiClient.postGenerateChallenge(basicAuth, ucrid);

        final List<Field> fields =
                supplementalInformationFormer.formChallengeResponseFields(
                        Key.LOGIN_DESCRIPTION, Key.LOGIN_INPUT, challengeResponse.getChallenge());

        storage.sessionStoreChallenge(challengeResponse.getChallenge());
        storage.sessionStoreActivationPassword(challengeResponse.getActivationPassword());
        storage.sessionStoreUcrid(ucrid);

        // Request supplemental info from card reader
        return new AuthenticationResponse(fields);
    }

    private static String generateUcrid() {
        return RandomStringUtils.randomNumeric(32);
    }
}
