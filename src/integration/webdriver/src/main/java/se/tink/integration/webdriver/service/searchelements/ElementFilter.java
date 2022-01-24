package se.tink.integration.webdriver.service.searchelements;

import org.openqa.selenium.WebElement;
import se.tink.integration.webdriver.WebDriverWrapper;
import se.tink.integration.webdriver.service.basicutils.WebDriverBasicUtils;

public interface ElementFilter {

    boolean matches(
            WebElement element, WebDriverWrapper driverWrapper, WebDriverBasicUtils basicUtils);
}
