package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.Storage;

@RequiredArgsConstructor
public class NemIdAuthenticationController implements TypedAuthenticator {

    private final NemIdIFrameController iFrameController;
    private final NemIdAuthenticatorV2 authenticator;
    private final Storage storage;

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {

        final String username = credentials.getField(Field.Key.USERNAME);
        final String pinCode = credentials.getField(Field.Key.ACCESS_PIN);
        final String password = credentials.getField(Field.Key.PASSWORD);

        if (Strings.isNullOrEmpty(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        final String token = iFrameController.doLoginWith(credentials);
        final String installId = authenticator.exchangeNemIdToken(token);

        authenticator.authenticateUsingInstallId(username, pinCode, installId);

        storage.put(NemIdConstantsV2.Storage.NEMID_INSTALL_ID, installId);
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.THIRD_PARTY_APP;
    }
}
