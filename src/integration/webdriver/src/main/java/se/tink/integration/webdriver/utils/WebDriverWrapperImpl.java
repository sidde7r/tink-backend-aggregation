package se.tink.integration.webdriver.utils;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@Slf4j
@RequiredArgsConstructor
public class WebDriverWrapperImpl implements WebDriverWrapper {

    private final WebDriver driver;
    private final Sleeper sleeper;

    @Override
    public void getUrl(String url) {
        driver.get(url);
    }

    @Override
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    @Override
    public void switchToParentWindow() {
        driver.switchTo().defaultContent();
    }

    @Override
    public boolean trySwitchToFrame(By frameSelector) {
        return tryFindElement(frameSelector)
                .map(
                        element -> {
                            driver.switchTo().frame(element);
                            return true;
                        })
                .orElse(false);
    }

    @Override
    public String getPageSource() {
        return driver.getPageSource();
    }

    @Override
    public Optional<WebElement> tryFindElement(By by) {
        return driver.findElements(by).stream().findFirst();
    }

    @Override
    public void quitDriver() {
        driver.quit();
    }
}
