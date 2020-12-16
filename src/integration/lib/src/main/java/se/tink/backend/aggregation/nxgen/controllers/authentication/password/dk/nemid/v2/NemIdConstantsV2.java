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

    static final long PHANTOMJS_TIMEOUT_SECONDS = 30;
    static final String NEM_ID_PREFIX = "[NemId]";

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Storage {
        public static final String NEMID_INSTALL_ID = "NEMID_INSTALL_ID";
    }

    static class Errors {
        static final ImmutableList<Pattern> INCORRECT_CREDENTIALS_ERROR_PATTERNS =
                ImmutableList.<Pattern>builder()
                        .add(
                                Pattern.compile("^incorrect (user|password).*"),
                                Pattern.compile("^fejl i (bruger|adgangskode).*"),
                                Pattern.compile("^indtast (bruger|adgangskode).*"))
                        .build();

        static final String ENTER_ACTIVATION_PASSWORD = "Enter activation password.";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static class HtmlElements {
        static final By USERNAME_INPUT = By.cssSelector("input[type=text]");
        static final By ERROR_MESSAGE = By.cssSelector("p.error");
        static final By NEMID_CODE_CARD = By.className("otp__card-number");
        static final By NEMID_CODE_TOKEN = By.className("otp__token");
        static final By PASSWORD_INPUT = By.cssSelector("input[type=password]");
        static final By SUBMIT_BUTTON = By.cssSelector("button.button--submit");
        static final By NEMID_TOKEN = By.cssSelector("div#tink_nemIdToken");
        static final By IFRAME = By.tagName("iframe");
        static final By OTP_ICON = By.className("otp__icon-phone-pulse");
        static final By NEMID_APP_BUTTON = By.cssSelector("button.button--submit");
    }
}
