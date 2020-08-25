package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config.SdcNoConstants.CARD_PORTAL_PATH;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config.AuthenticationType;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config.SdcNoConfiguration;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.libraries.selenium.WebDriverHelper;
import se.tink.libraries.selenium.exceptions.HtmlElementNotFoundException;

@Slf4j
public class PostAuthDriverProcessor {

    private final SdcNoConfiguration configuration;
    private final WebDriverHelper webDriverHelper;
    private final WebDriver driver;
    private final CookieManager cookieManager;

    private static final By TARGET_ELEMENT_XPATH = By.xpath("//input[@value='Logg ut']");

    public PostAuthDriverProcessor(
            WebDriver driver,
            WebDriverHelper webDriverHelper,
            TinkHttpClient client,
            SdcNoConfiguration configuration) {
        this.configuration = configuration;
        this.webDriverHelper = webDriverHelper;
        this.driver = driver;
        this.cookieManager = new CookieManager(driver, client);
    }

    public void processWebDriver() {
        cookieManager.setCookiesToClient();

        if (configuration.getAuthenticationType().equals(AuthenticationType.PORTAL)) {
            // loading additional website for more cookies necessary to fetch credit cards
            driver.get(configuration.getIndividualBaseURL() + CARD_PORTAL_PATH);
            try {
                webDriverHelper.getElement(driver, TARGET_ELEMENT_XPATH);
            } catch (HtmlElementNotFoundException ex) {
                log.info(
                        "Credit card portal not found, for URL: {}, source: {}",
                        driver.getCurrentUrl(),
                        driver.getPageSource());
                return;
            }
            cookieManager.setCookiesToClient();
        }
    }
}
