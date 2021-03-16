package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.BankIdConstants.HtmlSelectors.BY_IFRAME;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.BankIdConstants.HtmlSelectors.BY_IFRAME_SHADOW_HOST;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openqa.selenium.By;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementLocator;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BankIdConstants {

    public static final String BANK_ID_LOG_PREFIX = "[BankID]";

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HtmlSelectors {

        public static final By BY_IFRAME = By.xpath("//iframe[contains(@src, 'csfe')]");
        static final By BY_IFRAME_SHADOW_HOST = By.xpath("//div[@class='full_width_height']");
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HtmlLocators {
        /*
        enter social number screen
        */
        public static final BankIdElementLocator LOC_SSN_INPUT =
                inIframeLocator()
                        .element(new By.ByCssSelector("input[type=tel]"))
                        .mustBeDisplayed()
                        .build();

        private static BankIdElementLocator.Builder inIframeLocator() {
            return BankIdElementLocator.builder()
                    .iframe(BY_IFRAME)
                    .shadowHost(BY_IFRAME_SHADOW_HOST);
        }
    }
}
