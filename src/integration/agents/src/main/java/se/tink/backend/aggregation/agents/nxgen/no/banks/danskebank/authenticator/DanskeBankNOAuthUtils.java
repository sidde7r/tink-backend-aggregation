package se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank.authenticator;

import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openqa.selenium.WebDriver;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.integration.webdriver.ChromeDriverInitializer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DanskeBankNOAuthUtils {

    static void executeWithWebDriver(Consumer<WebDriver> webDriverConsumer) {
        WebDriver driver =
                ChromeDriverInitializer.constructChromeDriver(
                        DanskeBankConstants.Javascript.USER_AGENT);
        try {
            webDriverConsumer.accept(driver);
        } finally {
            ChromeDriverInitializer.quitChromeDriver(driver);
        }
    }
}
