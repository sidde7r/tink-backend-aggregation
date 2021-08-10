package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.authenticator.page;

import java.util.function.Consumer;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import se.tink.integration.webdriver.logger.HtmlLogger;

public final class Field {
    private final By query;

    private final Consumer<WebElement> consumer;

    private Field(By query, Consumer<WebElement> consumer) {
        this.query = query;
        this.consumer = consumer;
    }

    public static Field of(By query, Consumer<WebElement> consumer) {
        return new Field(query, consumer);
    }

    public WebElement apply(SearchContext searchContext, HtmlLogger htmlLogger) {
        return SearchContent.findElement(searchContext, query, htmlLogger)
                .andThenTry(consumer::accept)
                .get();
    }
}
