package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid;

import com.google.common.collect.ImmutableList;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openqa.selenium.By;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NemIdConstants {

    public static final String NEM_ID_APPLET_URL = "https://applet.danid.dk";
    public static final String NEM_ID_INIT_URL = NEM_ID_APPLET_URL + "/launcher/lmt/";

    public static final String USER_AGENT =
            "Mozilla/5.0 (iPhone; CPU iPhone OS 10_1_1 like Mac OS X) AppleWebKit/602.2.14 (KHTML, like Gecko) Mobile/14B100";

    public static final String NEM_ID_IFRAME_FORMAT =
            "<iframe id=\"nemid_iframe\" allowTransparency=\"true\" name=\"nemid_iframe\" scrolling=\"no\" style=\"z-index: 100; position: relative; width: 275px; height: 350px; border: 0\" src=\"%s\"></iframe>";

    // TODO: make sure Nordea will use the old one

    // This html is taken from Nordea DK iOS and android. It has been modified to only include
    // necessary parts.
    public static final String BASE_HTML =
            "<html>\n"
                    + "    <body>\n"
                    + "        %s\n"
                    + "        <script>\n"
                    + "            window.addEventListener(\"message\", function(e) {\n"
                    + "                var message = JSON.parse(e.data);\n"
                    + " console.log('test', message, JSON.stringify(message));\n"
                    + "               if (message.command === 'SendParameters') {\n"
                    + "                     var params = document.getElementById('nemid_parameters').innerHTML;\n"
                    + "                     var postMessage = {command: 'parameters', content: params };\n"
                    + "                     document.getElementById('nemid_iframe').contentWindow.postMessage(JSON.stringify(postMessage), 'https://applet.danid.dk');\n"
                    + "               } else if (message.command === \"changeResponseAndSubmit\" && message.content.length > 100) {\n"
                    + "                     if (!document.getElementById(\"tink_nemIdToken\")) {\n"
                    + "                         document.body.innerHTML += '<div id=\\\"tink_nemIdToken\\\"/>';\n"
                    + "                     }\n"
                    + "                     document.getElementById(\"tink_nemIdToken\").innerHTML = message.content;\n"
                    + "                    e.preventDefault();\n"
                    + "                }\n"
                    + "            });\n"
                    + "        </script>\n"
                    + "    </body>\n"
                    + "</html>";

    public static final String NEM_ID_PREFIX = "[NemId]";

    // value from docs
    private static final int NEM_ID_TIMEOUT_SECONDS = 5 * 60;
    // value increased by a few seconds just to be sure that we don't announce the timeout too early
    public static final int NEM_ID_TIMEOUT_SECONDS_WITH_SAFETY_MARGIN = NEM_ID_TIMEOUT_SECONDS + 15;

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Storage {
        public static final String NEMID_INSTALL_ID = "NEMID_INSTALL_ID";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Errors {

        /*
        Error messages under login & password inputs
         */
        public static final ImmutableList<Pattern> INCORRECT_CREDENTIALS_ERROR_PATTERNS =
                ImmutableList.of(
                        Pattern.compile("^incorrect (user|password).*"),
                        Pattern.compile("^fejl i (bruger|adgangskode).*"),
                        Pattern.compile("^indtast (bruger|adgangskode).*"),
                        Pattern.compile("^forkert adgangskode.*"));

        public static final ImmutableList<Pattern> ENTER_ACTIVATION_PASSWORD_PATTERNS =
                ImmutableList.of(
                        Pattern.compile("^enter activation password.*"),
                        Pattern.compile("^indtast midlertidig adgangskode.*"));

        public static final ImmutableList<Pattern>
                ENTER_NEMID_NUMBER_OR_SELF_CHOSEN_USER_ID_PATTERNS =
                        ImmutableList.of(
                                // add for en
                                Pattern.compile(
                                        "^log på med nemid-nr. eller selvvalgt bruger-id.*"));

        /*
        Error messages from NemId heading
         */
        public static final ImmutableList<Pattern> NEM_ID_REVOKED_PATTERNS =
                ImmutableList.of(
                        Pattern.compile("^nemid revoked.*"),
                        Pattern.compile("^nemid er spærret.*"));
        public static final ImmutableList<Pattern> USE_NEW_CODE_CARD_PATTERNS =
                ImmutableList.of(
                        Pattern.compile("^use new code card.*"),
                        Pattern.compile("^tag nyt nøglekort i brug.*"));
        public static final ImmutableList<Pattern> KEY_APP_NOT_READY_TO_USE_PATTERNS =
                ImmutableList.of(
                        // add for en
                        Pattern.compile("^nøgleapp ikke klar til brug*"));
        public static final ImmutableList<Pattern> NEMID_ISSUES_PATTERNS =
                ImmutableList.of(
                        // add for en
                        Pattern.compile("^nemid er blevet genåbnet automatisk.*"),
                        // add for en
                        Pattern.compile(
                                "^der opstod en fejl under oprettelsen af dit oces-certifikat.*"));
        public static final ImmutableList<Pattern> NEM_ID_RENEW_PATTERNS =
                ImmutableList.of(
                        // add for en
                        Pattern.compile("^vidste du, at nemid skal fornyes hvert tredje.*"));

        /*
        NemId code card & code token errors
         */
        public static final ImmutableList<Pattern> INVALID_CARD_OR_TOKEN_CODE_PATTERNS =
                ImmutableList.of(
                        Pattern.compile("^incorrect code.*"), Pattern.compile("^fejl i nøgle.*"));
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HtmlElements {
        /*
        Parent window
        */
        public static final By IFRAME = By.tagName("iframe");
        public static final By NOT_EMPTY_NEMID_TOKEN =
                By.xpath("//div[@id='tink_nemIdToken' and text()!='']");

        /*
        Common iframe elements
         */
        private static final String FRAME_CONTAINER_XPATH = "//*[@id='framecontentscroll']";

        // actionable button seems to always be the first button in the frame content
        private static final String SUBMIT_BUTTON_XPATH =
                String.format("(%s//button)[1]", FRAME_CONTAINER_XPATH);
        public static final By SUBMIT_BUTTON = By.xpath(SUBMIT_BUTTON_XPATH);

        // error message seems to be the last <p> before submit button
        public static final By NOT_EMPTY_ERROR_MESSAGE =
                By.xpath(SUBMIT_BUTTON_XPATH + "/preceding::div/p[text() != '']");

        // NOTE: this header is always present in the frame - it can contain a title for
        // authentication screen or some error screen. In order to detect error screen,
        // it should always be the LAST selector that we're looking for - otherwise we
        // won't detect anything else.
        public static final By NEMID_FRAME_HEADER =
                By.xpath(String.format("%s//h1[text() != ''][1]", FRAME_CONTAINER_XPATH));

        /*
        Login screen
         */
        public static final By USERNAME_INPUT = By.cssSelector("input[type=text]");
        public static final By PASSWORD_INPUT = By.cssSelector("input[type=password]");

        /*
        Code app screen
         */
        public static final By NEMID_CODE_APP_SCREEN_HEADER =
                By.xpath(
                        "//h1[contains(text(),'Authenticate using code app') or contains(text(),'Godkend med nøgleapp')]");
        public static final By NEMID_CODE_APP_TIMEOUT_HEADER =
                By.xpath(
                        "//h1[contains(text(), 'Your request has timed out') or contains(text(), 'Anmodningen er udløbet')]");

        /*
        Code card screen
         */
        public static final By NEMID_CODE_CARD_NUMBER =
                By.xpath(
                        "//div[contains(text(),'Code card') or contains(text(),'Nøglekort')]/span");

        // the code user has to provide
        private static final String NEMID_CODE_CARD_CODE_INPUT_XPATH =
                "//input[@type='tel' and @maxlength='6']";
        public static final By NEMID_CODE_CARD_CODE_INPUT =
                By.xpath(NEMID_CODE_CARD_CODE_INPUT_XPATH);

        // the number (#) of code that user has to provide
        public static final By NEMID_CODE_CARD_CODE_NUMBER =
                By.xpath(NEMID_CODE_CARD_CODE_INPUT_XPATH + "/preceding::div[1]");

        // IC-677 add for EN
        public static final By NEMID_RUNNING_OUT_OF_CODES_SCREEN_HEADER =
                By.xpath("//h1[contains(text(),'Nyt nøglekort')]");

        /*
        Code token screen
         */
        public static final By NEMID_CODE_TOKEN_SERIAL_NUMBER =
                By.xpath(
                        "//p[contains(text(),'Code token serial number') or contains(text(),'Serienummer på nøgleviser')]/following::span");

        public static final By NEMID_CODE_TOKEN_INPUT = By.xpath(NEMID_CODE_CARD_CODE_INPUT_XPATH);

        /*
        Change method link & popup
         */
        public static final By NEMID_SELECT_METHOD_POPUP_HEADER =
                By.xpath("//h1[text() = 'Select code type' or text() = 'Vælg nøgletype']");
        public static final By NEMID_LINK_TO_SELECT_DIFFERENT_2FA_METHOD =
                By.xpath("//a[text() = 'Change code type' or text() = 'Skift nøgletype']");
        public static final By NEMID_LINK_TO_CLOSE_SELECT_METHOD_POPUP =
                By.xpath("//a[text() = 'Back' or text() = 'Tilbage']");

        public static final By NEMID_SELECT_CODE_APP_BUTTON =
                By.xpath("//span[text() = 'Code app' or text() = 'Nøgleapp']/ancestor::button");
        public static final By NEMID_SELECT_CODE_CARD_BUTTON =
                By.xpath("//span[text() = 'Code card' or text() = 'Nøglekort']/ancestor::button");
        public static final By NEMID_SELECT_CODE_TOKEN_BUTTON =
                By.xpath("//span[text() = 'Code token' or text() = 'Nøgleviser']/ancestor::button");
    }

    // source:
    // https://www.nets.eu/dk-da/kundeservice/nemid-tjenesteudbyder/NemID-tjenesteudbyderpakken/Documents/NemID%20Error%20Codes.pdf
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class NemIdErrorCodes {

        public static final String TECHNICAL_ERROR = "SRV003";
        public static final String INTERNAL_ERROR = "SRV004";
        public static final String TIMEOUT = "SRV006";

        public static final String REJECTED = "CAN007";
        public static final String INTERRUPTED = "CAN008";

        public static final String NO_AGREEMENT = "AUTH003";
        public static final String NEMID_LOCKED = "AUTH004";
        public static final String NEMID_BLOCKED = "AUTH005";
        public static final String NEMID_PASSWORD_BLOCKED = "AUTH007";
        public static final String OLD_OTP_USED = "AUTH010";

        public static final String NETWORK_PROBLEM = "LIB002";
    }
}
