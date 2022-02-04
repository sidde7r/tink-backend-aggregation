package se.tink.integration.webdriver.service;

import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.integration.webdriver.WebDriverWrapper;
import se.tink.integration.webdriver.service.basicutils.WebDriverBasicUtils;
import se.tink.integration.webdriver.service.proxy.ProxyManager;
import se.tink.integration.webdriver.service.searchelements.ElementLocator;
import se.tink.integration.webdriver.service.searchelements.ElementsSearcher;

public interface WebDriverService
        extends WebDriverWrapper, WebDriverBasicUtils, ElementsSearcher, ProxyManager {

    void clickButton(ElementLocator selector);

    void setValueToElement(String value, ElementLocator selector);

    String getFullPageSourceLog(int maxIframeLevel);

    void terminate(AgentTemporaryStorage agentTemporaryStorage);
}
