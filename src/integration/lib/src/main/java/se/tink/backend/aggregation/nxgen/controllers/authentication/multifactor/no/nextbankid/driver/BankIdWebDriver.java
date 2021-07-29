package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver;

import java.util.Set;
import org.openqa.selenium.Cookie;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementLocator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementsSearcher;
import se.tink.integration.webdriver.WebDriverWrapper;

public interface BankIdWebDriver extends BankIdElementsSearcher {

    WebDriverWrapper getDriver();

    String getDriverId();

    void getUrl(String url);

    String getCurrentUrl();

    String getFullPageSourceLog();

    Set<Cookie> getCookies();

    void clickButton(BankIdElementLocator selector);

    void setValueToElement(String value, BankIdElementLocator selector);

    void sleepFor(int millis);
}
