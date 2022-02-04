package se.tink.integration.webdriver.service.basicutils;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebElement;

public interface WebDriverBasicUtils {

    Set<Cookie> getCookies();

    void switchToParentWindow();

    boolean trySwitchToIframe(By iframeSelector);

    boolean trySwitchToIframe(WebElement iframe);

    Optional<WebElement> tryFindElement(By by);

    Map<String, String> getElementAttributes(WebElement element);

    void sleepFor(int millis);

    boolean isElementVisible(WebElement element);
}
