package se.tink.libraries.selenium;

import com.google.common.base.Strings;
import java.util.List;
import java.util.Optional;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import se.tink.libraries.selenium.exceptions.HtmlElementNotFoundException;
import se.tink.libraries.selenium.exceptions.ScreenScrapingException;

public class WebDriverHelper {
    private static final By IFRAME_TAG = By.tagName("iframe");

    private final long waitForRenderInMillis;

    public WebDriverHelper() {
        this(2_000L);
    }

    WebDriverHelper(final long waitForRenderInMillis) {
        this.waitForRenderInMillis = waitForRenderInMillis;
    }

    public void sleep(long waitTime) {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public Optional<WebElement> waitForElement(WebDriver driver, By by) {
        for (int i = 0; i < 5; i++, sleep(waitForRenderInMillis)) {
            List<WebElement> elements = driver.findElements(by);
            if (!elements.isEmpty()) {
                return elements.stream().filter(WebElement::isDisplayed).findFirst();
            }
        }
        return Optional.empty();
    }

    public Optional<WebElement> waitForOneOfElements(WebDriver driver, By... byArray) {
        for (int i = 0; i < 5; i++, sleep(waitForRenderInMillis)) {
            for (By by : byArray) {
                List<WebElement> elements = driver.findElements(by);
                if (!elements.isEmpty()) {
                    return elements.stream().findFirst();
                }
            }
        }
        return Optional.empty();
    }

    public Optional<String> waitForElementWithAttribute(
            WebDriver driver, By elementPath, String attributeKey) {
        for (int i = 0; i < 10; i++, sleep(waitForRenderInMillis)) {
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
        for (int i = 0; i < 10; i++, sleep(waitForRenderInMillis)) {
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
