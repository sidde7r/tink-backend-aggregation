package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.authenticator;

import lombok.AllArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.CallbackProcessorEmpty;
import se.tink.integration.webdriver.WebDriverWrapper;

@AllArgsConstructor
public class BeforeMainPageAdvertisementPageProcessor implements CallbackProcessorEmpty {

    private final WebDriverWrapper webDriver;

    @Override
    public AuthenticationStepResponse process()
            throws AuthenticationException, AuthorizationException {
        try {
            WebElement globalPositionHyperlink =
                    webDriver.findElement(By.xpath("//div[@id='bot']/center/a[@class='bot']"));
            globalPositionHyperlink.click();
        } catch (NoSuchElementException ex) {
            // nothing to do as it seems there is no advertisement page
        }
        return AuthenticationStepResponse.executeNextStep();
    }
}
