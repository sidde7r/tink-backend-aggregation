package se.tink.integration.webdriver.service;

import static se.tink.integration.webdriver.service.WebDriverConstants.LOG_TAG;

import com.google.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.integration.webdriver.WebDriverWrapper;
import se.tink.integration.webdriver.service.basicutils.WebDriverBasicUtils;
import se.tink.integration.webdriver.service.proxy.ProxyManager;
import se.tink.integration.webdriver.service.searchelements.ElementLocator;
import se.tink.integration.webdriver.service.searchelements.ElementsSearchQuery;
import se.tink.integration.webdriver.service.searchelements.ElementsSearcher;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE, onConstructor = @__({@Inject}))
public class WebDriverServiceImpl implements WebDriverService {

    private static final String BREAK_LINE = "<br/>";
    private static final Pattern IFRAME_TAG_PATTERN = Pattern.compile("(?s)<iframe.*>.*</iframe>");

    @Delegate private final WebDriverWrapper driver;
    @Delegate private final WebDriverBasicUtils driverBasicUtils;
    @Delegate private final ElementsSearcher elementsSearcher;
    @Delegate private final ProxyManager proxyManager;

    @Override
    public void clickButton(ElementLocator locator) {
        try {
            /*
            Sometimes there is no effect of clicking the button. The exact reason was not investigated but an effective
            solution is to wait for a little to give page time to "stabilize" itself, e.g. recalculate some styles.
            It may be also helpful in avoiding org.openqa.selenium.ElementClickInterceptedException when something
            is briefly covering the button.
             */
            sleepFor(200);
            clickButtonInternal(locator);

        } catch (StaleElementReferenceException exception) {
            /*
            Sometimes element can become stale even though we have just found it. This may happen due to some
            style recalculations that cause element to be recreated.
            If such thing occurs, we should try to click button one more time.
             */
            log.warn("{} Stale button element reference", LOG_TAG);
            clickButtonInternal(locator);
        }
    }

    private void clickButtonInternal(ElementLocator locator) {
        WebElement buttonElement =
                elementsSearcher
                        .searchForFirstMatchingLocator(
                                ElementsSearchQuery.builder()
                                        .searchFor(locator)
                                        .searchForSeconds(10)
                                        .build())
                        .getFirstFoundElement()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Could not find button element by " + locator));

        buttonElement.click();
    }

    @Override
    public void setValueToElement(String value, ElementLocator locator) {
        WebElement element =
                elementsSearcher
                        .searchForFirstMatchingLocator(
                                ElementsSearchQuery.builder()
                                        .searchFor(locator)
                                        .searchForSeconds(10)
                                        .build())
                        .getFirstFoundElement()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Could not find element by " + locator));

        element.sendKeys(value);
    }

    @Override
    public String getFullPageSourceLog(int maxHeight) {
        StringBuilder stringBuilder = new StringBuilder();
        buildPageLog("html", Collections.emptyMap(), 0, 0, maxHeight, stringBuilder);
        return stringBuilder.toString();
    }

    @Override
    public void terminate(AgentTemporaryStorage agentTemporaryStorage) {
        proxyManager.shutDownProxy();
        agentTemporaryStorage.remove(driver.getDriverId());
    }

    void buildPageLog(
            String tag,
            Map<String, String> attributes,
            int height,
            int width,
            int maxHeight,
            StringBuilder stringBuilder) {
        if (height > maxHeight) {
            return;
        }

        String content = driver.getPageSource();
        content = commentOutIframeTag(content);

        stringBuilder
                .append("----------")
                .append(BREAK_LINE)
                .append("Tag: ")
                .append(tag)
                .append(BREAK_LINE)
                .append("Attributes: ")
                .append(attributes)
                .append(BREAK_LINE)
                .append("Tree location: height ")
                .append(height)
                .append(" width ")
                .append(width)
                .append(BREAK_LINE)
                .append("Page source: ")
                .append(content)
                .append(BREAK_LINE);

        // this will return only current top level iframes, without iframes nested inside them
        List<WebElement> iframes = driver.findElements(By.tagName("iframe"));
        for (int index = 0; index < iframes.size(); index++) {

            WebElement iframe = iframes.get(index);
            Map<String, String> iframeAttributes = driverBasicUtils.getElementAttributes(iframe);

            boolean switchedToIframe = driverBasicUtils.trySwitchToIframe(iframe);
            if (!switchedToIframe) {
                log.warn("{} Skipping iframe in page source - could not switch", LOG_TAG);
                continue;
            }
            buildPageLog("iframe", iframeAttributes, height + 1, index, maxHeight, stringBuilder);
            driver.switchTo().parentFrame();
        }
    }

    private String commentOutIframeTag(String pageContent) {
        Matcher matcher = IFRAME_TAG_PATTERN.matcher(pageContent);
        if (matcher.find()) {
            String iframeTag = matcher.group();
            pageContent = pageContent.replace(iframeTag, "<!--" + iframeTag + "-->");
        }
        return pageContent;
    }
}
