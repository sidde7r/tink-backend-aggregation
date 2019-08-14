package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.authenticator;

import com.google.common.base.Strings;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.authenticator.rpc.InitSecurityTokenChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.authenticator.rpc.SecurityTokenChallengeResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.i18n.Catalog;

public class SwedbankTokenGeneratorAuthenticationController implements MultiFactorAuthenticator {
    private final SwedbankDefaultApiClient apiClient;
    private final SupplementalInformationController supplementalInformationController;
    private final Catalog catalog;

    public SwedbankTokenGeneratorAuthenticationController(
            SwedbankDefaultApiClient apiClient,
            SupplementalInformationController supplementalInformationController,
            Catalog catalog) {
        this.apiClient = apiClient;
        this.supplementalInformationController = supplementalInformationController;
        this.catalog = catalog;
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        String ssn = "";
        if (credentials.hasField(Field.Key.USERNAME)) {
            ssn = credentials.getField(Field.Key.USERNAME);

            if (Strings.isNullOrEmpty(ssn)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }
        }

        initTokenGenerator(ssn);

        Map<String, String> supplementalInformation =
                supplementalInformationController.askSupplementalInformation(responseField());

        Optional<String> challenge =
                Optional.ofNullable(
                        supplementalInformation.get(
                                SwedbankBaseConstants.DeviceAuthentication.CHALLENGE));
        if (!challenge.isPresent()) {
            throw SupplementalInfoError.NO_VALID_CODE.exception();
        }

        SecurityTokenChallengeResponse securityTokenChallengeResponse =
                apiClient.sendLoginChallenge(challenge.get());
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
}
