package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver;

import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BankIdWebDriverConstants {

    public static final By EMPTY_BY =
            new By() {
                @Override
                public List<WebElement> findElements(SearchContext context) {
                    return Collections.emptyList();
                }
            };
}
