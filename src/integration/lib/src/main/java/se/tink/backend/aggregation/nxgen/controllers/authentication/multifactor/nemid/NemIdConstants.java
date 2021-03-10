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
                        Pattern.compile("^Forkert adgangskode.*"));

        public static final ImmutableList<Pattern> ENTER_ACTIVATION_PASSWORD_PATTERNS =
                ImmutableList.of(
                        Pattern.compile("^enter activation password.*"),
                        Pattern.compile("^Indtast midlertidig adgangskode.*"));

        public static final ImmutableList<Pattern> ENTER_6_DIGIT_PASSWORD_PATTERNS =
                ImmutableList.of(
                        // add for en
                        Pattern.compile("^Indtast 6-cifret nøgle*"));

        /*
        Error messages from NemId heading
         */
        public static final ImmutableList<Pattern> NEM_ID_REVOKED_PATTERNS =
                ImmutableList.of(
                        Pattern.compile("^nemid revoked.*"),
                        Pattern.compile("^NemID er spærret.*"));
        public static final ImmutableList<Pattern> USE_NEW_CODE_CARD_PATTERNS =
                ImmutableList.of(
                        Pattern.compile("^use new code card.*"),
                        Pattern.compile("^Tag nyt nøglekort i brug.*"));
        public static final ImmutableList<Pattern> KEY_APP_NOT_READY_TO_USE_PATTERNS =
                ImmutableList.of(
                        // add for en
                        Pattern.compile("^Nøgleapp ikke klar til brug*"));
        public static final ImmutableList<Pattern> NEMID_ISSUES_PATTERNS =
                ImmutableList.of(
                        // add for en
                        Pattern.compile("^NemID er blevet genåbnet automatisk*"),
                        // add for en
                        Pattern.compile(
                                "^Der opstod en fejl under oprettelsen af dit OCES-certifikat."));
        public static final ImmutableList<Pattern> NEM_ID_RENEW_PATTERNS =
                ImmutableList.of(
                        // add for en
                        Pattern.compile("^Vidste du, at NemID skal fornyes hvert tredje*"));

        /*
        NemId code card & code token errors
         */
        public static final ImmutableList<Pattern> INVALID_CARD_OR_TOKEN_CODE_PATTERNS =
                ImmutableList.of(
                        Pattern.compile("^incorrect code.*"), Pattern.compile("^fejl i nøgle.*"));
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HtmlElements {
        // Parent window
        public static final By IFRAME = By.tagName("iframe");
        public static final By NOT_EMPTY_NEMID_TOKEN =
                By.xpath("//div[@id='tink_nemIdToken' and text()!='']");

        // Common iframe elements
        public static final By SUBMIT_BUTTON = By.cssSelector("button.button--submit");
        public static final By NOT_EMPTY_ERROR_MESSAGE =
                By.xpath("//p[@class='error' and text()!='']");
        public static final By NEMID_WIDE_INFO_HEADING = By.xpath("//h1[@class='wide-heading'][1]");

        // Login screen
        public static final By USERNAME_INPUT = By.cssSelector("input[type=text]");
        public static final By PASSWORD_INPUT = By.cssSelector("input[type=password]");

        // 2FA method screens
        public static final By NEMID_CODE_APP_SCREEN = By.className("otp__icon-phone-pulse");
        public static final By NEMID_CODE_CARD_SCREEN = By.className("otp__card-number");
        public static final By NEMID_CODE_TOKEN_SCREEN = By.className("otp__token");
        public static final By NEMID_LINK_TO_SELECT_DIFFERENT_2FA_METHOD =
                By.xpath("//*[@class='frame__row']//a");
        public static final By NEMID_SELECT_METHOD_POPUP =
                By.xpath("//button[contains(@class, 'otp__iconTextButton')]");
        public static final By NEMID_CLOSE_SELECT_METHOD_POPUP =
                NEMID_LINK_TO_SELECT_DIFFERENT_2FA_METHOD;

        // Code app screen
        public static final By NEMID_TIMEOUT_ICON = By.className("otp__icon-noresponse");

        // Code card screen
        public static final By NEMID_CODE_CARD_NUMBER = NEMID_CODE_CARD_SCREEN;
        public static final By NEMID_CODE_CARD_CODE_NUMBER =
                By.xpath("//div[@class='otp__frame__row']/div[@class='otp__frame__cell'][1]");
        public static final By NEMID_CODE_CARD_CODE_INPUT =
                By.xpath(
                        "//div[@class='otp__frame__row']/div[@class='otp__frame__cell'][2]//input");

        // Code token screen
        public static final By NEMID_CODE_TOKEN_SERIAL_NUMBER =
                By.xpath("//*[@class='frame__row']//div[1]//span");
        public static final By NEMID_CODE_TOKEN_INPUT = By.xpath("//*[@class='input otp-input']");

        // Select 2FA methods popup
        public static final By NEMID_SELECT_CODE_APP_BUTTON =
                By.xpath("//button[./*[@class='icon-element otp__icon-phone']]");
        public static final By NEMID_SELECT_CODE_CARD_BUTTON =
                By.xpath("//button[./*[@class='icon-element otp__icon-keycard']]");
        public static final By NEMID_SELECT_CODE_TOKEN_BUTTON =
                By.xpath("//button[./*[@class='icon-element otp__icon-token']]");
    }

    // source:
    // https://www.nets.eu/dk-da/kundeservice/nemid-tjenesteudbyder/NemID-tjenesteudbyderpakken/Documents/NemID%20Error%20Codes.pdf
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class NemIdErrorCodes {

        public static final String TECHNICAL_ERROR = "SRV003";
        public static final String TIMEOUT = "SRV006";

        public static final String REJECTED = "CAN007";
        public static final String INTERRUPTED = "CAN008";

        public static final String NO_AGREEMENT = "AUTH003";
        public static final String NEMID_LOCKED = "AUTH004";
        public static final String NEMID_BLOCKED = "AUTH005";
        public static final String NEMID_PASSWORD_BLOCKED = "AUTH007";
    }
}
