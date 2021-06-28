package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.authenticator.page;

import io.vavr.CheckedConsumer;
import io.vavr.CheckedRunnable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.LoginForm;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.authenticator.HtmlLogger;

public final class Page {
    private final WebDriver webDriver;

    private final HtmlLogger htmlLogger;

    private List<Field> fields;

    private Page(WebDriver webDriver, HtmlLogger htmlLogger) {
        this.webDriver = webDriver;
        this.htmlLogger = htmlLogger;
        this.fields = Collections.emptyList();
    }

    public static Page of(WebDriver webDriver, HtmlLogger htmlLogger) {
        return new Page(webDriver, htmlLogger);
    }

    public boolean isError() {
        try {
            return SearchContent.findElement(webDriver, By.id(LoginForm.ERROR_PANEL), htmlLogger)
                    .map(WebElement::isDisplayed)
                    .get();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public Page fields(Field... fields) {
        this.fields = Arrays.asList(fields);
        return this;
    }

    public void submit(Supplier<By> query, long timeoutSeconds)
            throws AttemptsLimitExceededException {
        action(query, WebElement::submit, timeoutSeconds);
    }

    public void click(Supplier<By> query, long timeoutSeconds)
            throws AttemptsLimitExceededException {
        action(query, WebElement::click, timeoutSeconds);
    }

    private void action(
            Supplier<By> query, CheckedConsumer<WebElement> doOnPage, long timeoutSeconds)
            throws AttemptsLimitExceededException {
        String previousUrl = getCurrentUrl(webDriver);
        Retry.of(
                        () ->
                                SearchContent.findElement(webDriver, query.get(), htmlLogger)
                                        .andThenTry(applyFields())
                                        .andThenTry(doOnPage)
                                        .andThenTry(
                                                () ->
                                                        waitForErrorOrRedirect(
                                                                timeoutSeconds, previousUrl))
                                        .get(),
                        () -> webDriver.navigate().refresh())
                .call();
    }

    private CheckedRunnable applyFields() {
        return () -> fields.forEach(field -> field.apply(webDriver, htmlLogger));
    }

    private void waitForErrorOrRedirect(long timeoutSeconds, String previousUrl) {
        new WebDriverWait(webDriver, timeoutSeconds)
                .ignoring(StaleElementReferenceException.class)
                .until(
                        driver -> {
                            if (!getCurrentUrl(driver).equals(previousUrl)) {
                                return true;
                            }
                            return isError();
                        });
    }

    private static String getCurrentUrl(WebDriver webDriver) {
        return webDriver.getCurrentUrl();
    }
}
