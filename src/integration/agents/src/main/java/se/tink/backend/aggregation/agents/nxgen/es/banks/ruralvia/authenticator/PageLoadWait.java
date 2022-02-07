package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.authenticator;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import se.tink.integration.webdriver.WebDriverWrapper;

@Slf4j
@AllArgsConstructor
public class PageLoadWait {

    private WebDriverWrapper webDriver;

    void waitFor(final int secondsToWait) {
        ExpectedCondition<Boolean> pageLoadCondition =
                drive ->
                        ((JavascriptExecutor) drive)
                                .executeScript("return document.readyState")
                                .equals("complete");

        WebDriverWait wait = new WebDriverWait(webDriver, secondsToWait);
        wait.until(pageLoadCondition);
        log.info(String.format("Page %s has been load successfully", webDriver.getCurrentUrl()));
    }
}
