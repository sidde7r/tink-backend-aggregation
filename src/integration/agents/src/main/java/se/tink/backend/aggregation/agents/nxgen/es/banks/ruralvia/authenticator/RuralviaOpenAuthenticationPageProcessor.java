package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.authenticator;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.CallbackProcessorEmpty;
import se.tink.integration.webdriver.WebDriverWrapper;

@AllArgsConstructor
@Slf4j
public class RuralviaOpenAuthenticationPageProcessor implements CallbackProcessorEmpty {

    private final WebDriverWrapper webDriver;
    private final String mainPageUrl;

    @Override
    public AuthenticationStepResponse process()
            throws AuthenticationException, AuthorizationException {
        openRuralviaMainPage();
        findAndClickClientAccessButton();
        new PageLoadWait(webDriver).waitFor(5);
        return AuthenticationStepResponse.executeNextStep();
    }

    private void openRuralviaMainPage() {
        webDriver.get(mainPageUrl);
        new PageLoadWait(webDriver).waitFor(5);
    }

    private void findAndClickClientAccessButton() {
        WebElement loginPageHyperlink =
                webDriver.findElement(By.xpath("//li[@class='acceso-cliente']/a"));
        webDriver.executeScript("arguments[0].setAttribute('target','_self')", loginPageHyperlink);
        // element.click() throw exception (probably element is not visible). That is way click
        // action is forced by JavaScript
        webDriver.executeScript("arguments[0].click();", loginPageHyperlink);
    }
}
