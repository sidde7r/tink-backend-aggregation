package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.responsevalidator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.BelfiusBaseTest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.BelfiusLoginTestData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.LoginResponse;

public class LoginResponseValidatorTest extends BelfiusBaseTest {

    private LoginResponseValidator loginResponseValidator;

    @Before
    public void setUp() {
        loginResponseValidator = new LoginResponseValidator();
    }

    @Test
    public void shouldValidateGoodLoginResponse() {
        // given
        final LoginResponse loginResponse =
                getLoginResponse(BelfiusLoginTestData.GOOD_LOGIN_RESPONSE);

        // when
        final LoginResponseStatus resultStatus = loginResponseValidator.validate(loginResponse);

        // then
        assertThat(resultStatus).isEqualTo(LoginResponseStatus.NO_ERRORS);
    }

    @Test
    public void shouldHandleWrongCredentialsResponse() {
        // given
        final LoginResponse loginResponse =
                getLoginResponse(BelfiusLoginTestData.WRONG_CREDENTIALS);

        // when
        final LoginResponseStatus resultStatus = loginResponseValidator.validate(loginResponse);

        // then
        assertThat(resultStatus).isEqualTo(LoginResponseStatus.INCORRECT_CREDENTIALS);
    }

    @Test
    public void shouldHandleScaRequiredResponse() {
        // given
        final LoginResponse loginResponse =
                getLoginResponse(BelfiusLoginTestData.SCA_REQUIRED_DUE_TO_INACTIVITY);

        // when
        final LoginResponseStatus resultStatus = loginResponseValidator.validate(loginResponse);

        // then
        assertThat(resultStatus).isEqualTo(LoginResponseStatus.SESSION_EXPIRED);
    }
}
