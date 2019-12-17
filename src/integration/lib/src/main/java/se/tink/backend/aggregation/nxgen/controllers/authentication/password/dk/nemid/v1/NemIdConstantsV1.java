package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v1;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public class NemIdConstantsV1 {

    public static final URL BASE_URL = new URL("https://applet.danid.dk");

    public static class ErrorStrings {
        public static final String INVALID_CREDENTIALS = "Incorrect user ID or password";
        public static final String NEMID_NOT_ACTIVATED = "Enter activation password";
    }

    // This html is taken from Nordea DK iOS and android. It has been modified to only include
    // necessary parts.
    public static final String BASE_HTML =
            "<html>\n"
                    + "    <body>\n"
                    + "        %s\n"
                    + "        <script>\n"
                    + "            window.addEventListener(\"message\", function(e) {\n"
                    + "                var message = JSON.parse(e.data);\n"
                    + "                if (message.command === \"changeResponseAndSubmit\") {\n"
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
}
