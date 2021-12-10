package se.tink.integration.webdriver.service;

import org.openqa.selenium.By;
import se.tink.integration.webdriver.WebDriverWrapper;
import se.tink.integration.webdriver.service.basicutils.WebDriverBasicUtils;
import se.tink.integration.webdriver.service.proxy.ProxyManager;
import se.tink.integration.webdriver.service.searchelements.ElementLocator;
import se.tink.integration.webdriver.service.searchelements.ElementsSearcher;

public interface WebDriverService
        extends WebDriverWrapper, WebDriverBasicUtils, ElementsSearcher, ProxyManager {

    String getFullPageSourceLog(By iframeSelector);

    void clickButton(ElementLocator selector);

    void setValueToElement(String value, ElementLocator selector);

    void sleepFor(int millis);
}
