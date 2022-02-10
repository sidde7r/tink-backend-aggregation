package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.authenticator;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.connectivity.ConnectivityException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.CallbackProcessorEmpty;
import se.tink.connectivity.errors.ConnectivityErrorDetails;
import se.tink.integration.webdriver.WebDriverWrapper;

@AllArgsConstructor
@Slf4j
public class LoginResultValidationProcessor implements CallbackProcessorEmpty {

    private final WebDriverWrapper webDriver;

    @Override
    public AuthenticationStepResponse process()
            throws AuthenticationException, AuthorizationException {
        handleCredentialsIncorrectResponse();
        if (isLoginSuccess()) {
            return AuthenticationStepResponse.executeNextStep();
        }
        log.info("Unknown login result response:" + webDriver.getPageSource());
        throw new ConnectivityException(ConnectivityErrorDetails.TinkSideErrors.UNKNOWN_ERROR);
    }

    private void handleCredentialsIncorrectResponse() {
        if (isCredentialsIncorrectResponse()) {
            if (webDriver
                    .getPageSource()
                    .contains(
                            "Recuerda que, por seguridad, tres errores consecutivos bloquean el acceso. Tu usuario ha sido bloqueado.")) {
                throw new ConnectivityException(
                        ConnectivityErrorDetails.UserLoginErrors.USER_BLOCKED);
            }
            throw new ConnectivityException(
                    ConnectivityErrorDetails.UserLoginErrors.STATIC_CREDENTIALS_INCORRECT);
        }
    }

    private boolean isLoginSuccess() {
        return webDriver.getPageSource().contains("<div id=\"HEADER\">Posición Global</div>");
    }

    private boolean isCredentialsIncorrectResponse() {
        try {
            return webDriver.findElement(By.id("error_acceso")).isDisplayed();
        } catch (NoSuchElementException ex) {
            return false;
        }
    }
}
