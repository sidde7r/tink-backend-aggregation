package se.tink.integration.webdriver.service.basicutils;

import com.google.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.WebElement;
import se.tink.integration.webdriver.WebDriverWrapper;
import se.tink.integration.webdriver.service.WebDriverConstants;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class WebDriverBasicUtilsImpl implements WebDriverBasicUtils {

    private final WebDriverWrapper driver;
    private final Sleeper sleeper;

    @Override
    public Set<Cookie> getCookies() {
        return driver.manage().getCookies();
    }

    @Override
    public void switchToParentWindow() {
        driver.switchTo().defaultContent();
    }

    @Override
    public boolean trySwitchToIframe(By iframeSelector) {
        return tryFindElement(iframeSelector).map(this::trySwitchToIframe).orElse(false);
    }

    @Override
    public boolean trySwitchToIframe(WebElement iframe) {
        try {
            driver.switchTo().frame(iframe);
            return true;
        } catch (NoSuchFrameException e) {
            log.warn("{} Couldn't switch to iFrame", WebDriverConstants.LOG_TAG);
            return false;
        }
    }

    @Override
    public Optional<WebElement> tryFindElement(By by) {
        return driver.findElements(by).stream().findFirst();
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public Map<String, String> getElementAttributes(WebElement element) {
        String script =
                "var element = arguments[0];"
                        + "var items = {};"
                        + "for (index = 0; index < element.attributes.length; ++index) {"
                        + "   items[element.attributes[index].name] = element.attributes[index].value"
                        + "};"
                        + "return items;";
        return (Map<String, String>) driver.executeScript(script, element);
    }

    @Override
    public void sleepFor(int millis) {
        sleeper.sleepFor(millis);
    }

    @Override
    public boolean isElementVisible(WebElement element) {
        String script =
                ""
                        + "element = arguments[0]\n"
                        + "if (element == null) {\n"
                        + "    return false\n"
                        + "}\n"
                        + "\n"
                        + "const isDisplayed = !!element.offsetParent\n"
                        + "\n"
                        + "const elementComputedStyle = window.getComputedStyle(element)\n"
                        + "const isVisible = elementComputedStyle.visibility === 'visible'\n"
                        + "\n"
                        + "return isDisplayed && isVisible";
        return (boolean) driver.executeScript(script, element);
    }
}
