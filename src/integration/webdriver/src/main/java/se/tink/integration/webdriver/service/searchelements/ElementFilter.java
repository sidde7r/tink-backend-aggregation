package se.tink.integration.webdriver.service.searchelements;

import org.openqa.selenium.WebElement;

public interface ElementFilter {

    boolean matches(WebElement element);
}
