package se.tink.backend.aggregation.nxgen.controllers.authentication.html.loginvalidation.validators;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.connectivity.ConnectivityException;
import se.tink.connectivity.errors.ConnectivityErrorDetails;

public class LoginValidatorErrorMessageFinderTest {

    private ConnectivityException connectivityException =
            new ConnectivityException(
                    ConnectivityErrorDetails.UserLoginErrors.STATIC_CREDENTIALS_INCORRECT);

    @Test
    public void shouldThrowValidationException() {
        // given
        final String input = "wrapped test error message";
        LoginValidatorErrorMessageFinder objectUnderTest =
                new LoginValidatorErrorMessageFinder(connectivityException, "error message");

        // when
        Throwable throwable = Assertions.catchThrowable(() -> objectUnderTest.validate(input));

        // then
        Assertions.assertThat(throwable).isEqualTo(connectivityException);
    }

    @Test
    public void shouldPassValidation() {
        // given
        final String input = "wrapped test success message";
        LoginValidatorErrorMessageFinder objectUnderTest =
                new LoginValidatorErrorMessageFinder(connectivityException, "error message");

        // when
        Assertions.assertThatCode(() -> objectUnderTest.validate(input)).doesNotThrowAnyException();
    }
}
