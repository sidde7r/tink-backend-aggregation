package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.authenticator.page;

import static io.vavr.API.$;
import static io.vavr.Predicates.instanceOf;

import io.vavr.API;
import io.vavr.control.Try;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.authenticator.HtmlLogger;

public interface SearchContent {

    static Try<WebElement> findElement(
            SearchContext searchContext, By query, HtmlLogger htmlLogger) {
        return Try.of(() -> searchContext.findElement(query))
                .mapFailure(
                        API.Case(
                                $(instanceOf(NoSuchElementException.class)),
                                ex -> {
                                    htmlLogger.error(
                                            String.format(
                                                    "Could not find an element `%s`",
                                                    query.toString()));
                                    return ex;
                                }));
    }
}
