package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator;

import com.google.common.base.Strings;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.rpc.InitSecurityTokenChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.rpc.SecurityTokenChallengeResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.type.AuthenticationControllerType;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SwedbankTokenGeneratorAuthenticationController
        implements TypedAuthenticator, AuthenticationControllerType {
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

        String challengeResponse = supplementalInformationHelper.waitForLoginInput();
        if (Strings.isNullOrEmpty(challengeResponse)
                || challengeResponse.length() != 8
                || !challengeResponse.matches("[0-9]+")) {
            throw SupplementalInfoError.NO_VALID_CODE.exception();
        }

        SecurityTokenChallengeResponse securityTokenChallengeResponse =
                apiClient.sendTokenChallengeResponse(
                        initSecurityTokenChallengeResponse.getLinks().getNextOrThrow(),
                        challengeResponse,
                        SecurityTokenChallengeResponse.class);
        apiClient.completeAuthentication(
                securityTokenChallengeResponse.getLinks().getNextOrThrow());
    }

    private InitSecurityTokenChallengeResponse initTokenGenerator(String ssn) {
        return apiClient.initTokenGenerator(ssn);
    }

    @Override
    public boolean isManualAuthentication(CredentialsRequest request) {
        // since always asks for login input
        return true;
    }
}
