package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.passwordandapp;

import com.google.common.base.Strings;
import java.util.Objects;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.i18n.Catalog;

public class AppAuthenticationPasswordController<T>
        extends ThirdPartyAppAuthenticationController<T> {

    private final Catalog catalog;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final PasswordAuthenticator passwordAuthentication;

    public AppAuthenticationPasswordController(
            Catalog catalog,
            PasswordAuthenticator passwordAuthenticator,
            ThirdPartyAppAuthenticator<T> appAuthenticator,
            SupplementalInformationHelper supplementalInformationHelper) {
        super(appAuthenticator, supplementalInformationHelper);
        this.catalog = catalog;
        this.passwordAuthentication = passwordAuthenticator;
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    @Override
    public CredentialsTypes getType() {
        // TODO: Change to a multifactor type when supported
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        NotImplementedException.throwIf(
                !Objects.equals(credentials.getType(), getType()),
                String.format(
                        "Authentication method not implemented for CredentialsType: %s",
                        credentials.getType()));

        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);

        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        passwordAuthentication.authenticate(username, password);

        super.authenticate(credentials);
    }
}
