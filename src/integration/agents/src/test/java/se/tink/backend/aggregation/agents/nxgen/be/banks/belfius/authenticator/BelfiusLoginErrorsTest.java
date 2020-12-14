package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.responsevalidator.LoginResponseStatus;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.responsevalidator.LoginResponseValidator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.PrepareLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.MessageResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BelfiusLoginErrorsTest {

    @Test
    public void shouldHandleNotSubscribedToMobile() throws LoginException {
        // given
        PrepareLoginResponse prepareLoginResponse =
                SerializationUtils.deserializeFromString(
                        BelfiusLoginErrorData.NOT_ENABLED_MOBILE_BANKING,
                        PrepareLoginResponse.class);
        MessageResponse.validate(prepareLoginResponse);

        // when
        Throwable t = catchThrowable(prepareLoginResponse::validate);

        // then
        assertThat(t)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.REGISTER_DEVICE_ERROR");
    }

    @Test
    public void shouldHandleSessionDoesNotExists() {
        // given
        LoginResponse loginResponse =
                SerializationUtils.deserializeFromString(
                        BelfiusLoginErrorData.SESSION_DOES_NOT_EXISTS, LoginResponse.class);

        // when
        LoginResponseStatus loginResponseStatus = LoginResponseValidator.validate(loginResponse);

        // then
        assertThat(loginResponseStatus).isEqualTo(LoginResponseStatus.SESSION_EXPIRED);
    }

    @Test
    public void shouldHandleUnknownSession() {
        // given
        LoginResponse loginResponse =
                SerializationUtils.deserializeFromString(
                        BelfiusLoginErrorData.UNKNOWN_SESSION, LoginResponse.class);

        // when
        LoginResponseStatus loginResponseStatus = LoginResponseValidator.validate(loginResponse);

        // then
        assertThat(loginResponseStatus).isEqualTo(LoginResponseStatus.SESSION_EXPIRED);
    }
}
