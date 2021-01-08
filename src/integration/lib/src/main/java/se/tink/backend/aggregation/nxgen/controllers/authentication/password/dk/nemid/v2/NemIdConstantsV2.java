package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2;

import com.google.common.collect.ImmutableList;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openqa.selenium.By;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NemIdConstantsV2 {

    public static final String NEM_ID_APPLET_URL = "https://applet.danid.dk";
    public static final String NEM_ID_INIT_URL = NEM_ID_APPLET_URL + "/launcher/lmt/";

    public static final String USER_AGENT =
            "Mozilla/5.0 (iPhone; CPU iPhone OS 10_1_1 like Mac OS X) AppleWebKit/602.2.14 (KHTML, like Gecko) Mobile/14B100";

    public static final String NEM_ID_IFRAME =
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

    public static final long PHANTOMJS_TIMEOUT_SECONDS = 30;
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
        public static final ImmutableList<Pattern> INCORRECT_CREDENTIALS_ERROR_PATTERNS =
                ImmutableList.<Pattern>builder()
                        .add(
                                Pattern.compile("^incorrect (user|password).*"),
                                Pattern.compile("^fejl i (bruger|adgangskode).*"),
                                Pattern.compile("^indtast (bruger|adgangskode).*"))
                        .build();

        public static final String ENTER_ACTIVATION_PASSWORD = "Enter activation password.";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HtmlElements {
        public static final By IFRAME = By.tagName("iframe");

        public static final By USERNAME_INPUT = By.cssSelector("input[type=text]");
        public static final By PASSWORD_INPUT = By.cssSelector("input[type=password]");
        public static final By SUBMIT_LOGIN_BUTTON = By.cssSelector("button.button--submit");

        public static final By NEMID_CODE_APP_METHOD = By.className("otp__icon-phone-pulse");
        public static final By NEMID_CODE_CARD_METHOD = By.className("otp__card-number");
        public static final By NEMID_CODE_TOKEN_METHOD = By.className("otp__token");
        public static final By NOT_EMPTY_ERROR_MESSAGE =
                By.xpath("//p[@class='error' and text()!='']");
        public static final By NEMID_TOKEN = By.cssSelector("div#tink_nemIdToken");

        public static final By NEMID_CODE_APP_BUTTON = By.cssSelector("button.button--submit");

        public static final By NEMID_TIMEOUT_ICON = By.className("otp__icon-noresponse");
    }

    // source:
    // https://www.nets.eu/dk-da/kundeservice/nemid-tjenesteudbyder/NemID-tjenesteudbyderpakken/Documents/NemID%20Error%20Codes.pdf
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class NemIdErrorCodes {
        public static final String TIMEOUT = "SRV006";
        public static final String REJECTED = "CAN007";
    }
}
