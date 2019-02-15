package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator;

import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.PrepareLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.MessageResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BelfiusLoginErrorsTest {

    @Test(expected = LoginException.class)
    public void shouldHandleNotSubscribedToMobile() throws LoginException {
        PrepareLoginResponse prepareLoginResponse =
                SerializationUtils.deserializeFromString(
                        BelfiusLoginErrorData.NOT_ENABLED_MOBILE_BANKING, PrepareLoginResponse.class);
        MessageResponse.validate(prepareLoginResponse);
        prepareLoginResponse.validate();
    }

    @Test(expected = LoginException.class)
    public void shouldHandleWrongCredentials()
            throws AuthenticationException, AuthorizationException {
        LoginResponse loginResponse =
                SerializationUtils.deserializeFromString(BelfiusLoginErrorData.WRONG_CREDENTIALS, LoginResponse.class);
        MessageResponse.validate(loginResponse);
        loginResponse.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldHandleSessionDoesNotExists() {
        LoginResponse loginResponse =
                SerializationUtils.deserializeFromString(
                        BelfiusLoginErrorData.SESSION_DOES_NOT_EXISTS, LoginResponse.class);
        MessageResponse.validate(loginResponse);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldHandleUnknownSession() {
        LoginResponse loginResponse =
                SerializationUtils.deserializeFromString(BelfiusLoginErrorData.UNKNOWN_SESSION, LoginResponse.class);
        MessageResponse.validate(loginResponse);
    }
}
