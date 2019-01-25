package se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.authenticator;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.rpc.Field;

public class BnpPfAuthenticator implements Authenticator {

    @Override
    public void authenticate(Credentials credentials) throws AuthenticationException, AuthorizationException {
        String accessToken = credentials.getField(Field.Key.ACCESS_TOKEN);

        if (Strings.isNullOrEmpty(accessToken)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }
}
