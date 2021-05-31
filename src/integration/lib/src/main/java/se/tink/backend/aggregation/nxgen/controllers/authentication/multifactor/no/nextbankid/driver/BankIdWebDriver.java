package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver;

import java.util.Set;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementLocator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementsSearcher;

public interface BankIdWebDriver extends BankIdElementsSearcher {

    WebDriver getDriver();

    void getUrl(String url);

    String getCurrentUrl();

    void quitDriver();

    String getFullPageSourceLog();

    Set<Cookie> getCookies();

    void clickButton(BankIdElementLocator selector);

    void setValueToElement(String value, BankIdElementLocator selector);

    void sleepFor(int millis);
}
