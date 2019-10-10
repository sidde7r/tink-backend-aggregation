package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator;

import java.util.UUID;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.GenerateOtpChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.LogonResponse;
import se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.Digipass;
import se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.models.OtpChallengeResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public final class AxaCommonAuthenticator {
    public static void authenticate(final AxaApiClient apiClient, final AxaStorage storage)
            throws AuthorizationException {

        final String serialNo = storage.getSerialNo().orElseThrow(IllegalStateException::new);
        final UUID deviceId = storage.getDeviceId().orElseThrow(IllegalStateException::new);
        final String basicAuth = storage.getBasicAuth().orElseThrow(IllegalStateException::new);

        final GenerateOtpChallengeResponse otpChallengeResponse =
                apiClient.postGenerateOtpChallenge(basicAuth, serialNo);

        // Can be removed once we're certain we don't need it
        storage.persistOptChallengeResponse(otpChallengeResponse);

        final String otpChallenge = otpChallengeResponse.getChallenge();

        final Digipass digipass = storage.getDigipass().orElseThrow(IllegalStateException::new);
        final OtpChallengeResponse digipassResponse =
                digipass.generateResponseFromChallenge(otpChallenge);

        // Can be removed once we're certain we don't need it
        storage.persistDigiOtpChallengeResponse(digipassResponse);

        final String password = digipassResponse.getOtpResponse();

        final LogonResponse logonResponse;
        try {
            logonResponse = apiClient.postLogon(basicAuth, deviceId.toString(), serialNo, password);
        } catch (HttpResponseException e) {
            final ErrorResponse response = e.getResponse().getBody(ErrorResponse.class);
            if (response.isPermanentlyBlocked()) {
                // Happens if the user has permanently un-registered their device using e.g. the
                // webpage. The user would have to create a new credential.
                throw AuthorizationError.ACCOUNT_BLOCKED.exception(e);
            }
            throw e;
        }

        // Can be removed once we're certain we don't need it
        storage.persistLogonResponse(logonResponse);

        final String accessToken =
                logonResponse.getAccessToken().orElseThrow(IllegalStateException::new);

        storage.sessionStoreAccessToken(accessToken);
    }
}
