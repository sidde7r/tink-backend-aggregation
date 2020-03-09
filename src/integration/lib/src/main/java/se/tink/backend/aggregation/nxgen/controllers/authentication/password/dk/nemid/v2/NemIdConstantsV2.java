package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public class NemIdConstantsV2 {

    public static final String NEM_ID_INIT_URL = "https://applet.danid.dk/launcher/lmt/";
    public static final URL BASE_URL = new URL("https://applet.danid.dk");
    public static final String NEM_ID_IFRAME =
            "<iframe id=\"nemid_iframe\" allowTransparency=\"true\" name=\"nemid_iframe\" scrolling=\"no\" style=\"z-index: 100; position: relative; width: 275px; height: 350px; border: 0\" src=\"%s\"></iframe>";

    public static class ErrorStrings {
        public static final String INVALID_CREDENTIALS = "Incorrect user ID or password";
        public static final String NEMID_NOT_ACTIVATED = "Enter activation password";
    }

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

    // iOS 10.1.1
    public static final String USER_AGENT =
            "Mozilla/5.0 (iPhone; CPU iPhone OS 10_1_1 like Mac OS X) AppleWebKit/602.2.14 (KHTML, like Gecko) Mobile/14B100";

    public static class Storage {
        public static final String NEMID_INSTALL_ID = "NEMID_INSTALL_ID";
    }
}
