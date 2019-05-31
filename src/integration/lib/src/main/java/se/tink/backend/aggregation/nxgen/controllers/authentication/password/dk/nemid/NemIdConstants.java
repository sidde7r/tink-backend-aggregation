package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid;

import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.URL;

public class NemIdConstants {

    public static final URL BASE_URL = new URL("https://applet.danid.dk");

    public static final LogTag NEM_ID_AUTH_ERROR_TAG = LogTag.from("nem-id-auth-error");

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
