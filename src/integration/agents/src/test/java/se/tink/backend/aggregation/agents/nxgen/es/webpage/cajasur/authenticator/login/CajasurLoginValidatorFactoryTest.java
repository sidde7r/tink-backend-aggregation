package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.connectivity.ConnectivityException;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.CajasurTestConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.html.loginvalidation.validators.LoginValidationExecutor;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.connectivity.errors.ConnectivityErrorDetails;

public class CajasurLoginValidatorFactoryTest {

    private SessionStorage sessionStorage = new SessionStorage();
    private CajasurLoginValidatorFactory objectUnderTest =
            new CajasurLoginValidatorFactory(sessionStorage);

    @Test
    public void shouldThrowCredentialsIncorrectError() throws IOException {
        // given
        String responseBody =
                FileUtils.readFileToString(
                        Paths.get(
                                        CajasurTestConstants.TEST_DATA_PATH,
                                        "login_incorrect_credentials_response.html")
                                .toFile(),
                        StandardCharsets.UTF_8);
        LoginValidationExecutor<String> loginValidationStep =
                new LoginValidationExecutor<>(objectUnderTest);

        // when
        Throwable throwable =
                Assertions.catchThrowable(() -> loginValidationStep.execute(responseBody));

        // then
        Assertions.assertThat(throwable).isInstanceOf(ConnectivityException.class);
        Assertions.assertThat(
                        ((ConnectivityException) throwable).getError().getDetails().getReason())
                .isEqualTo(
                        ConnectivityErrorDetails.UserLoginErrors.STATIC_CREDENTIALS_INCORRECT
                                .name());
    }

    @Test
    public void shouldValidateSuccessLogin() throws IOException {
        // given
        String responseBody =
                FileUtils.readFileToString(
                        Paths.get(
                                        CajasurTestConstants.TEST_DATA_PATH,
                                        "login_success_response.html")
                                .toFile(),
                        StandardCharsets.UTF_8);
        LoginValidationExecutor<String> loginValidationStep =
                new LoginValidationExecutor<>(objectUnderTest);

        // when
        Assertions.assertThatCode(() -> loginValidationStep.execute(responseBody))
                .doesNotThrowAnyException();
    }
}
