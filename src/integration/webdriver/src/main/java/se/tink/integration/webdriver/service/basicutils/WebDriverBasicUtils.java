package se.tink.integration.webdriver.service.basicutils;

import java.util.Optional;
import java.util.Set;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebElement;

public interface WebDriverBasicUtils {

    Set<Cookie> getCookies();

    void switchToParentWindow();

    boolean trySwitchToIframe(By iframeSelector);

    Optional<WebElement> tryFindElement(By by);

    void sleepFor(int millis);
}
