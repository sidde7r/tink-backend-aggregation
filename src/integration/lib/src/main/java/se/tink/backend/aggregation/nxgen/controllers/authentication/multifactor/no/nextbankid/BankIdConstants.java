package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlSelectors.BY_BUTTON_LINK;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlSelectors.BY_ERROR_SCREEN_WITH_HEADING;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlSelectors.BY_ERROR_SCREEN_WITH_HEADING_TEXT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlSelectors.BY_ERROR_SCREEN_WITH_NO_HEADING;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlSelectors.BY_IFRAME;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlSelectors.BY_IFRAME_SHADOW_HOST;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlSelectors.BY_NON_ERROR_SCREEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlSelectors.BY_SUBMIT_BUTTON;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.By.ByCssSelector;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementLocator;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BankIdConstants {

    public static final String BANK_ID_LOG_PREFIX = "[BankID]";
    public static final int DEFAULT_WAIT_FOR_ELEMENT_TIMEOUT_IN_SECONDS = 10;
    /*
    Those values are based on timeouts observed in tests with ambassadors.
    Every value is rounded up to multiple of 30s + there is 30s added just to be sure.
     */
    public static final int MOBILE_BANK_ID_TIMEOUT_IN_SECONDS = 120;
    public static final int BANK_ID_APP_TIMEOUT_IN_SECONDS = 150;

    public static final int WAIT_FOR_AUTHENTICATION_FINISH_SIGNS_IN_SECONDS = 5;

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HtmlSelectors {

        public static final By BY_IFRAME = By.xpath("//iframe[contains(@src, 'csfe')]");
        static final By BY_IFRAME_SHADOW_HOST = By.xpath("//div[@class='full_width_height']");

        static final ByCssSelector BY_SUBMIT_BUTTON = new ByCssSelector("form button[type=submit]");
        static final ByCssSelector BY_SSN_INPUT = new ByCssSelector("input[type=tel]");
        static final ByCssSelector BY_NON_ERROR_SCREEN = new ByCssSelector("form label");
        static final ByCssSelector BY_ERROR_SCREEN_WITH_HEADING =
                new ByCssSelector(".message > h2");
        static final ByCssSelector BY_ERROR_SCREEN_WITH_HEADING_TEXT =
                new ByCssSelector(".message > p");
        static final ByCssSelector BY_ERROR_SCREEN_WITH_NO_HEADING = new ByCssSelector(".message");
        static final ByCssSelector BY_BUTTON_LINK = new ByCssSelector("button.link");
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HtmlLocators {

        public static final BankIdElementLocator LOC_IFRAME =
                BankIdElementLocator.builder().iframe(BY_IFRAME).build();
        public static final BankIdElementLocator LOC_SUBMIT_BUTTON =
                inIframeLocator().element(BY_SUBMIT_BUTTON).mustBeDisplayed().build();

        /*
        enter social number screen
        */
        public static final BankIdElementLocator LOC_ENTER_SSN_SCREEN =
                nonErrorScreenLocator().mustContainOneOfTexts("Fødselsnummer", "User ID").build();
        public static final BankIdElementLocator LOC_SSN_INPUT =
                inIframeLocator().element(HtmlSelectors.BY_SSN_INPUT).mustBeDisplayed().build();

        /*
        mobile BankID screen
        */
        public static final BankIdElementLocator LOC_MOBILE_BANK_ID_METHOD_SCREEN =
                nonErrorScreenLocator()
                        .mustContainOneOfTexts("BankID på mobil", "BankID on mobile")
                        .build();
        public static final BankIdElementLocator LOC_REFERENCE_WORDS =
                inIframeLocator()
                        .element(new ByCssSelector("span[data-bind='text: reference']"))
                        .mustBeDisplayed()
                        .build();

        /*
        chip code screen
        */
        public static final BankIdElementLocator LOC_CHIP_CODE_METHOD_SCREEN =
                nonErrorScreenLocator()
                        .mustContainOneOfTexts("Engangskode", "One Time Code")
                        .build();
        public static final BankIdElementLocator LOC_CHIP_CODE_INPUT =
                inIframeLocator()
                        .element(new By.ByCssSelector("input[type=password]"))
                        .mustBeDisplayed()
                        .build();

        /*
        BankID app screen
        */
        public static final BankIdElementLocator LOC_BANK_ID_APP_METHOD_SCREEN =
                nonErrorScreenLocator()
                        .mustContainOneOfTexts("Engangskode på mobil app", "BankID-app")
                        .build();

        /*
        error screens
        */
        public static final BankIdElementLocator LOC_BANK_ID_ERROR_WITH_HEADING_SCREEN =
                inIframeLocator()
                        .element(BY_ERROR_SCREEN_WITH_HEADING)
                        .mustContainOneOfTexts(
                                "Det har oppstått en feil.", "An error has occurred.")
                        .build();
        public static final BankIdElementLocator LOC_BANK_ID_ERROR_WITH_HEADING_TEXT =
                inIframeLocator().element(BY_ERROR_SCREEN_WITH_HEADING_TEXT).build();

        public static final BankIdElementLocator LOC_BANK_ID_ERROR_NO_HEADING_SCREEN =
                inIframeLocator()
                        .element(BY_ERROR_SCREEN_WITH_NO_HEADING)
                        .mustContainOneOfTexts(
                                "Det har oppstått en feil.", "An error has occurred.")
                        .build();
        public static final BankIdElementLocator LOC_BANK_ID_ERROR_NO_HEADING_TEXT =
                LOC_BANK_ID_ERROR_NO_HEADING_SCREEN;

        /*
        enter password screen
        */
        public static final BankIdElementLocator LOC_PRIVATE_PASSWORD_SCREEN =
                nonErrorScreenLocator()
                        .mustContainOneOfTexts("Personlig passord", "Personal password")
                        .build();
        public static final BankIdElementLocator LOC_PRIVATE_PASSWORD_INPUT =
                inIframeLocator()
                        .element(new By.ByCssSelector("input[type=password][data-type=password]"))
                        .mustBeDisplayed()
                        .build();
        public static final BankIdElementLocator LOC_PRIVATE_PASSWORD_ERROR_BUBBLE =
                inIframeLocator().element(new ByCssSelector(".infobubble_wrapper .text")).build();

        /*
        common 2FA elements
         */
        public static final BankIdElementLocator LOC_CHANGE_2FA_METHOD_LINK =
                inIframeLocator().element(BY_BUTTON_LINK).mustBeDisplayed().build();

        private static BankIdElementLocator.Builder nonErrorScreenLocator() {
            return inIframeLocator().element(BY_NON_ERROR_SCREEN);
        }

        private static BankIdElementLocator.Builder inIframeLocator() {
            return BankIdElementLocator.builder()
                    .iframe(BY_IFRAME)
                    .shadowHost(BY_IFRAME_SHADOW_HOST);
        }
    }
}
