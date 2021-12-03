package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.basicutils.WebDriverBasicUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.ElementLocator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.ElementsSearcher;
import se.tink.integration.webdriver.WebDriverWrapper;

public interface BankIdWebDriver extends WebDriverWrapper, WebDriverBasicUtils, ElementsSearcher {

    String getFullPageSourceLog();

    void clickButton(ElementLocator selector);

    void setValueToElement(String value, ElementLocator selector);

    void sleepFor(int millis);
}
