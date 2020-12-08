package se.tink.libraries.selenium;

import com.google.common.base.Strings;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import se.tink.libraries.selenium.exceptions.HtmlElementNotFoundException;
import se.tink.libraries.selenium.exceptions.ScreenScrapingException;

public class WebDriverHelper {
    private static final long WAIT_FOR_RENDER_MILLIS = 2000;
    private static final By IFRAME_TAG = By.tagName("iframe");
    private static final File phantomJsdriverFile;

    static {
        boolean mac = System.getProperty("os.name").toLowerCase().contains("mac");

        if (mac) {
            phantomJsdriverFile = new File("tools/phantomjs-tink-mac64-2.1.1");
        } else {
            phantomJsdriverFile = new File("tools/phantomjs-tink-linux-x86_64-2.1.1");
        }
    }

    public WebDriver constructPhantomJsWebDriver(String userAgent) {
        return constructPhantomJsWebDriver(userAgent, false, "");
    }

    public WebDriver constructPhantomJsWebDriver(
            String userAgent, boolean withDebugLogs, String proxyServer) {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(
                PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                phantomJsdriverFile.getAbsolutePath());

        capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, false);
        capabilities.setCapability(CapabilityType.SUPPORTS_ALERTS, false);

        List<String> cliArguments = new ArrayList<>();

        // To allow iframe-hacking.
        cliArguments.add("--web-security=false");
        // No need to load images.
        cliArguments.add("--load-images=false");

        if (withDebugLogs) {
            cliArguments.add("--debug=true");
            cliArguments.add("--webdriver-loglevel=DEBUG");
        }

        if (!Strings.isNullOrEmpty(proxyServer)) {
            cliArguments.add("--proxy-type=http");
            // E.g.: `http://127.0.0.1:8080`
            cliArguments.add("--proxy=" + proxyServer);
            cliArguments.add("--ignore-ssl-errors=true");
        }

        capabilities.setCapability(
                PhantomJSDriverService.PHANTOMJS_CLI_ARGS, cliArguments.toArray(new String[0]));
        capabilities.setCapability(
                PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent", userAgent);
        return new PhantomJSDriver(capabilities);
    }

    public void sleep(long waitTime) {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public Optional<WebElement> waitForElement(WebDriver driver, By by) {
        for (int i = 0; i < 5; i++, sleep(WAIT_FOR_RENDER_MILLIS)) {
            List<WebElement> elements = driver.findElements(by);
            if (!elements.isEmpty()) {
                return elements.stream().filter(WebElement::isDisplayed).findFirst();
            }
        }
        return Optional.empty();
    }

    public Optional<WebElement> waitForOneOfElements(WebDriver driver, By... byArray) {
        for (int i = 0; i < 5; i++, sleep(WAIT_FOR_RENDER_MILLIS)) {
            for (By by : byArray) {
                List<WebElement> elements = driver.findElements(by);
                if (!elements.isEmpty()) {
                    return elements.stream().filter(WebElement::isDisplayed).findFirst();
                }
            }
        }
        return Optional.empty();
    }

    public Optional<String> waitForElementWithAttribute(
            WebDriver driver, By elementPath, String attributeKey) {
        for (int i = 0; i < 10; i++, sleep(WAIT_FOR_RENDER_MILLIS)) {
            Optional<String> attributeValue =
                    waitForElement(driver, elementPath)
                            .map(el -> el.getAttribute(attributeKey))
                            .filter(val -> !Strings.isNullOrEmpty(val));

            if (attributeValue.isPresent()) {
                return attributeValue;
            }
        }
        return Optional.empty();
    }

    public void switchToIframe(WebDriver driver) {
        driver.switchTo().defaultContent();
        WebElement iframeElement = getElement(driver, IFRAME_TAG);
        driver.switchTo().frame(iframeElement);
    }

    public void sendInputValue(WebElement element, String value) {
        if (!checkIfElementEnabledIfNotWait(element)) {
            throw new ScreenScrapingException(
                    String.format("Element %s is not interactable", element.toString()));
        }
        element.sendKeys(value);
    }

    public void submitForm(WebDriver driver, By by) {
        for (int i = 0; i < 10; i++) {
            try {
                getElement(driver, by).submit();
                break;
            } catch (StaleElementReferenceException exception) {
                // NOOP; try again.
            }
        }
    }

    public WebElement getElement(WebDriver driver, By by) {
        return waitForElement(driver, by)
                .orElseThrow(
                        () ->
                                new HtmlElementNotFoundException(
                                        String.format("Can't find element %s", by.toString())));
    }

    public void clickButton(WebElement submitButton) {
        if (!checkIfElementEnabledIfNotWait(submitButton)) {
            throw new ScreenScrapingException(
                    String.format("Button %s is not interactable", submitButton.toString()));
        }
        submitButton.sendKeys(Keys.ENTER);
    }

    public boolean checkIfElementEnabledIfNotWait(WebElement element) {
        for (int i = 0; i < 10; i++, sleep(WAIT_FOR_RENDER_MILLIS)) {
            try {
                if (element.isEnabled()) {
                    return true;
                }
            } catch (StaleElementReferenceException e) {
                // NOOP; try again.
            }
        }
        return false;
    }
}
