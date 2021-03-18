package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openqa.selenium.By;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BankIdConstants {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HtmlElements {

        public static final By BY_IFRAME = By.xpath("//iframe[contains(@src, 'csfe')]");
    }
}
