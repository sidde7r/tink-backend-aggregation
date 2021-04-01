package se.tink.integration.webdriver.utils;

import java.util.Optional;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public interface WebDriverWrapper {

    void getUrl(String url);

    String getCurrentUrl();

    void switchToParentWindow();

    boolean trySwitchToFrame(By frameSelector);

    String getPageSource();

    Optional<WebElement> tryFindElement(By by);

    void quitDriver();
}
