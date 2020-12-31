package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.rpc.InitSecurityTokenChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.rpc.SecurityTokenChallengeResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;

/**
 * This class handles Swedbank's security token authentication. Swedbank provides two types of
 * security tokens:
 *
 * <p>Orange security token: Equipped with camera that user scans a control image with, this
 * security token is currently not supported. For users with this token a LoginException will be
 * thrown.
 *
 * <p>Black security token: Has two ways of login, one where the user generates an OTP with their
 * token, and one where the user receives a challenge that they input in their security token. Both
 * login flows for this token is supported.
 */
public class SwedbankTokenGeneratorAuthenticationController implements TypedAuthenticator {
    private static final Logger log =
            LoggerFactory.getLogger(SwedbankTokenGeneratorAuthenticationController.class);

    private final SwedbankDefaultApiClient apiClient;
    private final SupplementalInformationHelper supplementalInformationHelper;

    public SwedbankTokenGeneratorAuthenticationController(
            SwedbankDefaultApiClient apiClient,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.apiClient = apiClient;
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        String ssn = credentials.getField(Field.Key.USERNAME);
        if (Strings.isNullOrEmpty(ssn)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        InitSecurityTokenChallengeResponse initSecurityTokenChallengeResponse =
                initTokenGenerator(ssn);

        String challengeResponse = getChallengeResponse(initSecurityTokenChallengeResponse);

        if (Strings.isNullOrEmpty(challengeResponse)
                || challengeResponse.length() != 8
                || !challengeResponse.matches("[0-9]+")) {
            throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
        }

        SecurityTokenChallengeResponse securityTokenChallengeResponse =
                apiClient.sendLoginTokenChallengeResponse(
                        initSecurityTokenChallengeResponse.getLinks().getNextOrThrow(),
                        challengeResponse);
        apiClient.completeAuthentication(
                securityTokenChallengeResponse.getLinks().getNextOrThrow());
    }

    public String getChallengeResponse(
            InitSecurityTokenChallengeResponse initSecurityTokenChallengeResponse)
            throws SupplementalInfoException, LoginException {
        String challengeResponse;

        if (initSecurityTokenChallengeResponse.isUseOneTimePassword()) {
            challengeResponse = supplementalInformationHelper.waitForLoginInput();
        } else {
            challengeResponse = executeChallengeExchangeFlow(initSecurityTokenChallengeResponse);
        }

        return challengeResponse;
    }

    public String executeChallengeExchangeFlow(
            InitSecurityTokenChallengeResponse initSecurityTokenChallengeResponse)
            throws LoginException, SupplementalInfoException {
        log.info("User has security token with challenge exchange login flow");

        String challenge = initSecurityTokenChallengeResponse.getChallenge();

        // Image challenge present means that user has a security token with camera, a flow
        // we currently don't support.
        if (initSecurityTokenChallengeResponse.getImageChallenge() != null) {
            throw LoginError.NOT_SUPPORTED.exception();
        }

        if (Strings.isNullOrEmpty(challenge)) {
            throw new IllegalStateException("Expected login challenge to be present");
        }

        return supplementalInformationHelper.waitForSignCodeChallengeResponse(challenge);
    }

    private InitSecurityTokenChallengeResponse initTokenGenerator(String ssn) {
        return apiClient.initTokenGenerator(ssn);
    }
}
