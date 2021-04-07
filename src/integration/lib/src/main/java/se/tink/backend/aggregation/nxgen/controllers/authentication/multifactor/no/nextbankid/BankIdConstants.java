package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlSelectors.BY_IFRAME;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlSelectors.BY_IFRAME_SHADOW_HOST;

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

        static final By.ByCssSelector BY_SSN_INPUT = new By.ByCssSelector("input[type=tel]");
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HtmlLocators {
        /*
        enter social number screen
        */
        public static final BankIdElementLocator LOC_SSN_INPUT =
                inIframeLocator().element(HtmlSelectors.BY_SSN_INPUT).mustBeDisplayed().build();

        private static BankIdElementLocator.Builder inIframeLocator() {
            return BankIdElementLocator.builder()
                    .iframe(BY_IFRAME)
                    .shadowHost(BY_IFRAME_SHADOW_HOST);
        }
    }
}
