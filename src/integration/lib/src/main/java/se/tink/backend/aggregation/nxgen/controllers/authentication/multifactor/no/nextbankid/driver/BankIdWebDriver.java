package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver;

import java.util.Set;
import org.openqa.selenium.Cookie;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.ElementLocator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.ElementsSearcher;
import se.tink.integration.webdriver.WebDriverWrapper;

public interface BankIdWebDriver extends ElementsSearcher {

    WebDriverWrapper getDriver();

    String getDriverId();

    void getUrl(String url);

    String getCurrentUrl();

    String getFullPageSourceLog();

    Set<Cookie> getCookies();

    void clickButton(ElementLocator selector);

    void setValueToElement(String value, ElementLocator selector);

    void sleepFor(int millis);
}
