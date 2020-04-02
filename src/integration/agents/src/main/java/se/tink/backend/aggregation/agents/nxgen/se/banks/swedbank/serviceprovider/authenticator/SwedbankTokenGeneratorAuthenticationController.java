package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator;

import com.google.common.base.Strings;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.rpc.InitSecurityTokenChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.rpc.SecurityTokenChallengeResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.type.AuthenticationControllerType;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.i18n.Catalog;

public class SwedbankTokenGeneratorAuthenticationController
        implements TypedAuthenticator, AuthenticationControllerType {
    private final SwedbankDefaultApiClient apiClient;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final Catalog catalog;

    public SwedbankTokenGeneratorAuthenticationController(
            SwedbankDefaultApiClient apiClient,
            SupplementalInformationHelper supplementalInformationHelper,
            Catalog catalog) {
        this.apiClient = apiClient;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.catalog = catalog;
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

        String challenge = supplementalInformationHelper.waitForLoginInput();
        if (Strings.isNullOrEmpty(challenge)
                || challenge.length() != 8
                || !challenge.matches("[0-9]+")) {
            throw SupplementalInfoError.NO_VALID_CODE.exception();
        }

        SecurityTokenChallengeResponse securityTokenChallengeResponse =
                apiClient.sendLoginChallenge(
                        initSecurityTokenChallengeResponse.getLinks(), challenge);
        apiClient.completeAuthentication(
                securityTokenChallengeResponse.getLinks().getNextOrThrow());
    }

    private InitSecurityTokenChallengeResponse initTokenGenerator(String ssn) {
        return apiClient.initTokenGenerator(ssn);
    }

    private Field responseField() {
        return Field.builder()
                .description(catalog.getString("Security Token"))
                .name(SwedbankBaseConstants.DeviceAuthentication.CHALLENGE)
                .numeric(true)
                .hint("NNNNNNNN")
                .maxLength(8)
                .minLength(8)
                .pattern("([0-9]{8})")
                .helpText(
                        catalog.getString(
                                "Start your code generator and write your PIN code."
                                        + " When APPLI is shown on the display press the key labeled 1 on your generator."
                                        + " Write the response code shown on the display in the field below."))
                .build();
    }

    @Override
    public boolean isManualAuthentication(CredentialsRequest request) {
        // since always asks for login input
        return true;
    }
}
