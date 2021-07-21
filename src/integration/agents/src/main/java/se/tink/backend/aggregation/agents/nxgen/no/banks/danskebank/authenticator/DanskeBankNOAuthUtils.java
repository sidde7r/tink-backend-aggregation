package se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank.authenticator;

import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openqa.selenium.WebDriver;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.integration.webdriver.ChromeDriverConfig;
import se.tink.integration.webdriver.ChromeDriverInitializer;
import se.tink.integration.webdriver.WebDriverWrapper;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DanskeBankNOAuthUtils {

    static void executeWithWebDriver(Consumer<WebDriver> webDriverConsumer) {
        WebDriverWrapper driver =
                ChromeDriverInitializer.constructChromeDriver(
                        ChromeDriverConfig.builder()
                                .userAgent(DanskeBankConstants.Javascript.USER_AGENT)
                                .build());
        try {
            webDriverConsumer.accept(driver);
        } finally {
            ChromeDriverInitializer.quitChromeDriver(driver);
        }
    }
}
