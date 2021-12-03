package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver;

import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.agents.utils.log.LogTag;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WebDriverConstants {

    public static final LogTag LOG_TAG = LogTag.from("[WebDriver]");

    public static final int DEFAULT_WAIT_FOR_ELEMENT_TIMEOUT_IN_SECONDS = 10;

    public static final By EMPTY_BY =
            new By() {
                @Override
                public List<WebElement> findElements(SearchContext context) {
                    return Collections.emptyList();
                }
            };
}
