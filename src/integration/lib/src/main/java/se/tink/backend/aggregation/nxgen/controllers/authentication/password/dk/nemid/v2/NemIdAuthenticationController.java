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
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class NemIdAuthenticationController
        implements TypedAuthenticator, AuthenticationControllerType {

    private static final String NEMID_PASSWORD_FIELD_NAME = "nemid-password";

    private final NemIdIFrameController iFrameController;
    private final NemIdAuthenticatorV2 authenticator;
    private final Storage storage;

    public NemIdAuthenticationController(
            NemIdIFrameController iFrameController,
            NemIdAuthenticatorV2 authenticator,
            Storage storage) {
        this.iFrameController = iFrameController;
        this.authenticator = authenticator;
        this.storage = storage;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {

        final String username = credentials.getField(Field.Key.USERNAME);
        final String pinCode = credentials.getField(Field.Key.ACCESS_PIN);
        final String password = credentials.getField(NEMID_PASSWORD_FIELD_NAME);

        if (Strings.isNullOrEmpty(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        final String token = iFrameController.doLoginWith(username, password);
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
