package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.utils;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants.Authentication;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.rpc.ChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.rpc.SolveChallengeRequest;
import se.tink.libraries.cryptography.hash.Hash;
import se.tink.libraries.encoding.EncodingUtils;

public class SpankkiAuthUtils {

    private final SpankkiApiClient apiClient;

    public SpankkiAuthUtils(SpankkiApiClient apiClient) {
        this.apiClient = apiClient;
    }

    // Requests challenge from server that has to be solved before continuing with the auth flow
    public void solveChallenge() {
        final ChallengeResponse challengeResponse = apiClient.receiveChallenge();
        final SolveChallengeRequest solveChallengeRequest =
                createSolveChallengeRequest(challengeResponse);
        apiClient.solveChallenge(solveChallengeRequest);
    }

    private SolveChallengeRequest createSolveChallengeRequest(ChallengeResponse challengeResponse) {
        final String authenticationId = challengeResponse.getAuthenticationId();
        final String challenge = challengeResponse.getChallenge();
        final String challengeResponseCode = calculateChallengeResponseCode(challenge);

        return new SolveChallengeRequest(authenticationId, challengeResponseCode);
    }

    private String calculateChallengeResponseCode(String challenge) {
        final String challengeBytesString = challenge + Authentication.CHALLENGE_RESPONSE_HASH_SALT;

        return EncodingUtils.encodeAsBase64String(Hash.sha256(challengeBytesString));
    }
}
