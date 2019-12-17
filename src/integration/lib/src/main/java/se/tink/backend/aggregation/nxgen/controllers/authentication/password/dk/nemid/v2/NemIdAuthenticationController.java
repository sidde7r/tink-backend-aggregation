package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2;

import com.google.common.base.Strings;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AuthenticationControllerType;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class NemIdAuthenticationController
        implements TypedAuthenticator, AuthenticationControllerType {

    private static final String SUPPLEMENTAL_FIELD_ID = "online-banking-password";

    private final NemIdIFrameController iFrameController;
    private final NemIdAuthenticatorV2 authenticator;
    private final Storage storage;
    private final SupplementalInformationHelper supplementalInformationHelper;

    public NemIdAuthenticationController(
            NemIdIFrameController iFrameController,
            NemIdAuthenticatorV2 authenticator,
            Storage storage,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.iFrameController = iFrameController;
        this.authenticator = authenticator;
        this.storage = storage;
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    private Field getField() {
        return Field.builder()
                .name(SUPPLEMENTAL_FIELD_ID)
                .description(
                        "During your first login you will need to authenticate using the NemId app. Please enter your online banking password and then open the NemId app.")
                .hint("Online Banking Password")
                .masked(true)
                .numeric(false)
                .build();
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {

        final String username = credentials.getField(Field.Key.USERNAME);
        final String pinCode = credentials.getField(Field.Key.ACCESS_PIN);
        final String password =
                supplementalInformationHelper
                        .askSupplementalInformation(getField())
                        .get(SUPPLEMENTAL_FIELD_ID);

        if (Strings.isNullOrEmpty(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        final NemIdParametersV2 nemIdParameters = authenticator.getNemIdParameters();
        final String token = iFrameController.doLoginWith(username, password, nemIdParameters);
        final String installId = authenticator.exchangeNemIdToken(token);

        authenticator.authenticateUsingInstallId(username, pinCode, installId);

        storage.put(NemIdConstantsV2.Storage.NEMID_INSTALL_ID, installId);
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.THIRD_PARTY_APP;
    }

    @Override
    public boolean isManualAuthentication(CredentialsRequest request) {
        return true;
    }
}
