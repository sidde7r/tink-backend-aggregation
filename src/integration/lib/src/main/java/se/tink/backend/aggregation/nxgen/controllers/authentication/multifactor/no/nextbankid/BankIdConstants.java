package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlSelectors.BY_BUTTON_LINK;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlSelectors.BY_CHOOSE_2FA_METHOD_OPTIONS_LIST;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlSelectors.BY_CHOOSE_2FA_METHOD_OPTION_BUTTON_LABEL;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlSelectors.BY_ERROR_SCREEN_WITH_HEADING;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlSelectors.BY_ERROR_SCREEN_WITH_HEADING_TEXT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlSelectors.BY_ERROR_SCREEN_WITH_NO_HEADING;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlSelectors.BY_IFRAME;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlSelectors.BY_MOBILE_BANK_ID_SPINNER;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlSelectors.BY_ONE_TIME_CODE_INPUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlSelectors.BY_PASSWORD_ERROR_BUBBLE;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlSelectors.BY_PASSWORD_INPUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlSelectors.BY_REFERENCE_WORDS;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlSelectors.BY_SSN_INPUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlSelectors.BY_SUBMIT_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlSelectors.BY_THIRD_PARTY_APP_SPINNER;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlSelectors.BY_TITLE_OF_SCREEN_WITH_FORM_TO_SEND;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriverConstants.EMPTY_BY;

import java.util.function.Function;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.By.ByCssSelector;
import se.tink.backend.aggregation.agents.utils.supplementalfields.NorwegianFields.BankIdOneTimeCodeField;
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
    public static final int THIRD_PARTY_APP_TIMEOUT_IN_SECONDS = 150;

    public static final int WAIT_FOR_SIGN_THAT_AUTHENTICATION_IS_FINISHED_FOR_SECONDS = 5;

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Validation {

        public static final Pattern VALID_SSN_PATTERN = Pattern.compile("^\\d{11}$");
        public static final Pattern VALID_ONE_TIME_CODE_PATTERN =
                Pattern.compile(BankIdOneTimeCodeField.VALID_OTP_PATTERN);
        /*
        Length limits based on https://www.bankid.no/en/private/solve-my-bankid-problem/faq/
         */
        public static final Pattern VALID_PASSWORD_PATTERN = Pattern.compile("^.{6,255}$");
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HtmlSelectors {

        public static final ByCssSelector BY_IFRAME = new ByCssSelector("iframe[src*='csfe']");

        static final ByCssSelector BY_SUBMIT_BUTTON = new ByCssSelector("form button[type=submit]");
        static final ByCssSelector BY_TITLE_OF_SCREEN_WITH_FORM_TO_SEND =
                new ByCssSelector("form label");
        static final ByCssSelector BY_ERROR_SCREEN_WITH_HEADING =
                new ByCssSelector(".message > h2");
        static final ByCssSelector BY_ERROR_SCREEN_WITH_HEADING_TEXT =
                new ByCssSelector(".message > p");
        static final ByCssSelector BY_ERROR_SCREEN_WITH_NO_HEADING = new ByCssSelector(".message");
        static final ByCssSelector BY_BUTTON_LINK = new ByCssSelector("form button.link");
        static final ByCssSelector BY_REFERENCE_WORDS =
                new ByCssSelector("span[data-bind='text: reference']");
        static final ByCssSelector BY_PASSWORD_ERROR_BUBBLE =
                new ByCssSelector(".infobubble_wrapper .text");
        static final ByCssSelector BY_CHOOSE_2FA_METHOD_OPTIONS_LIST =
                new ByCssSelector(".otp-list");
        static final ByCssSelector BY_CHOOSE_2FA_METHOD_OPTION_BUTTON_LABEL =
                new ByCssSelector(".otp-list li button span.label");

        /*
         * These inputs must be mutually exclusive
         */
        static final ByCssSelector BY_SSN_INPUT = new ByCssSelector("input[type=tel]");
        static final ByCssSelector BY_ONE_TIME_CODE_INPUT =
                new ByCssSelector("input[type=password]:not([data-type]):not([disabled])");
        static final ByCssSelector BY_PASSWORD_INPUT =
                new ByCssSelector("input[type=password][data-type=password]:not([disabled])");
        /*
         * These spinners must be mutually exclusive
         */
        static final ByCssSelector BY_MOBILE_BANK_ID_SPINNER =
                new ByCssSelector(".icon[src] + .spinner");
        static final ByCssSelector BY_THIRD_PARTY_APP_SPINNER =
                new ByCssSelector(".icon:not([src]) + .spinner");
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HtmlLocators {

        public static final BankIdElementLocator LOC_IFRAME =
                BankIdElementLocator.builder().element(BY_IFRAME).build();
        public static final BankIdElementLocator LOC_SUBMIT_BUTTON =
                inIframeLocator().element(BY_SUBMIT_BUTTON).mustBeDisplayed().build();

        /*
        enter social number screen
        */
        public static final BankIdElementLocator LOC_ENTER_SSN_SCREEN =
                inIframeLocator().element(BY_SSN_INPUT).build();
        public static final BankIdElementLocator LOC_SSN_INPUT =
                inIframeLocator().element(BY_SSN_INPUT).mustBeDisplayed().build();

        /*
        mobile BankID screen
        */
        public static final BankIdElementLocator LOC_MOBILE_BANK_ID_METHOD_SCREEN =
                inIframeLocator()
                        .element(BY_TITLE_OF_SCREEN_WITH_FORM_TO_SEND)
                        .mustContainOneOfTexts("BankID på mobil", "BankID on mobile")
                        .build();
        public static final BankIdElementLocator LOC_MOBILE_BANK_ID_REFERENCE_WORDS_SCREEN =
                inIframeLocator().element(BY_MOBILE_BANK_ID_SPINNER).build();
        public static final BankIdElementLocator LOC_REFERENCE_WORDS =
                inIframeLocator().element(BY_REFERENCE_WORDS).mustBeDisplayed().build();

        /*
        one-time code screen
        */
        public static final BankIdElementLocator LOC_ONE_TIME_CODE_METHOD_SCREEN =
                inIframeLocator().element(BY_ONE_TIME_CODE_INPUT).build();
        public static final BankIdElementLocator LOC_ONE_TIME_CODE_INPUT =
                inIframeLocator().element(BY_ONE_TIME_CODE_INPUT).mustBeDisplayed().build();

        /*
        third party app screen
        */
        public static final BankIdElementLocator LOC_THIRD_PARTY_APP_METHOD_SCREEN =
                inIframeLocator().element(BY_THIRD_PARTY_APP_SPINNER).build();

        /*
        choose 2FA screen
         */
        public static final BankIdElementLocator LOC_CHOOSE_2FA_METHOD_SCREEN =
                inIframeLocator().element(BY_CHOOSE_2FA_METHOD_OPTIONS_LIST).build();
        public static final BankIdElementLocator LOC_CHOOSE_2FA_METHOD_OPTION_BUTTON_LABEL =
                inIframeLocator().element(BY_CHOOSE_2FA_METHOD_OPTION_BUTTON_LABEL).build();
        public static final Function<String, BankIdElementLocator>
                LOC_CHOOSE_2FA_METHOD_OPTION_BUTTON_WITH_LABEL =
                        label ->
                                inIframeLocator()
                                        .element(BY_CHOOSE_2FA_METHOD_OPTION_BUTTON_LABEL)
                                        .mustHaveExactText(label)
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
                inIframeLocator().element(BY_PASSWORD_INPUT).build();
        public static final BankIdElementLocator LOC_PRIVATE_PASSWORD_INPUT =
                inIframeLocator().element(BY_PASSWORD_INPUT).mustBeDisplayed().build();
        public static final BankIdElementLocator LOC_PRIVATE_PASSWORD_ERROR_BUBBLE =
                inIframeLocator().element(BY_PASSWORD_ERROR_BUBBLE).build();

        /*
        common 2FA elements
         */
        public static final BankIdElementLocator LOC_CHANGE_2FA_METHOD_LINK =
                inIframeLocator().element(BY_BUTTON_LINK).mustBeDisplayed().build();

        private static BankIdElementLocator.Builder inIframeLocator() {
            return BankIdElementLocator.builder()
                    .iframe(BY_IFRAME)
                    .shadowHost(getIframeShadowHost());
        }

        /*
        Recently BankID iframe implementation for Chromium switched back to the version without using shadow DOMs.
        However, it can be changed in a future. Previous selector for host element for shadow DOM inside iframe was:

        By.xpath("//div[@class='full_width_height']")
        */
        private static By getIframeShadowHost() {
            return EMPTY_BY;
        }
    }
}
