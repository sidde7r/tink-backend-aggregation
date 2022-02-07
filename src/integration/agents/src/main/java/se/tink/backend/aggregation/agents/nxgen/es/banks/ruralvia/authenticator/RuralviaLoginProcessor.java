package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.authenticator;

import java.util.List;
import lombok.AllArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.CallbackProcessorEmpty;
import se.tink.integration.webdriver.WebDriverWrapper;

@AllArgsConstructor
public class RuralviaLoginProcessor implements CallbackProcessorEmpty {

    private final WebDriverWrapper webDriver;
    private final Credentials credentials;

    @Override
    public AuthenticationStepResponse process()
            throws AuthenticationException, AuthorizationException {
        WebElement loginForm = webDriver.findElement(By.id("form1"));
        List<WebElement> inputElements = loginForm.findElements(By.tagName("input"));
        fillInCredentials(inputElements);
        loginForm.submit();
        return AuthenticationStepResponse.executeNextStep();
    }

    private void fillInCredentials(final List<WebElement> inputElements) {
        inputElements.get(0).sendKeys(credentials.getField(Field.Key.USERNAME));
        inputElements.get(1).sendKeys(credentials.getField(Field.Key.NATIONAL_ID_NUMBER));
        inputElements.get(2).sendKeys(credentials.getField(Field.Key.PASSWORD));
    }
}
