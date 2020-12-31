package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.authenticator;

import com.google.common.base.Strings;
import org.apache.http.HttpStatus;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.authenticator.rpc.ChallengeResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class SebTokenGenratorAuthenticationController implements TypedAuthenticator {
    private final SebApiClient apiClient;
    private final SupplementalInformationHelper supplementalInformationHelper;

    public SebTokenGenratorAuthenticationController(
            SebApiClient apiClient, SupplementalInformationHelper supplementalInformationHelper) {
        this.apiClient = apiClient;
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        String ssn = credentials.getField(Key.USERNAME);
        ChallengeResponse challenge = apiClient.getChallenge();
        String signature =
                supplementalInformationHelper.waitForSignCodeChallengeResponse(
                        challenge.getD1() + challenge.getD2());

        if (Strings.isNullOrEmpty(signature)
                || signature.length() != 6
                || !signature.matches("[0-9]+")) {
            throw SupplementalInfoError.NO_VALID_CODE.exception();
        }

        try {
            apiClient.verifyChallengeSolution(signature, ssn.substring(2));
        } catch (HttpResponseException e) {
            switch (e.getResponse().getStatus()) {
                case HttpStatus.SC_FORBIDDEN:
                    throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
                    // Login successfull
                case HttpStatus.SC_NO_CONTENT:
                    apiClient.setupSession(ssn);
                    return;
                default:
                    throw LoginError.INCORRECT_CREDENTIALS.exception();
            }
        }

        // Just in case SEB decides to return 200 in the future
        apiClient.setupSession(ssn);
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }
}
