package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator;

import org.junit.Ignore;

@Ignore
public class SwedbankTokenGeneratorAuthenticationControllerTestData {

    private SwedbankTokenGeneratorAuthenticationControllerTestData() {
        throw new IllegalStateException("Utility class");
    }

    public static final String SECURITY_TOKEN_OTP =
            "{\n"
                    + "  \"useOneTimePassword\": true,\n"
                    + "  \"challenge\": \"\",\n"
                    + "  \"links\": {\n"
                    + "    \"next\": {\n"
                    + "      \"method\": \"POST\",\n"
                    + "      \"uri\": \"/v5/identification/securitytoken\"\n"
                    + "    }\n"
                    + "  }\n"
                    + "}";

    public static final String SECURITY_TOKEN_CHALLENGE =
            "{\n"
                    + "  \"useOneTimePassword\": false,\n"
                    + "  \"challenge\": \"11111111\",\n"
                    + "  \"annotatedDescription\": \"Börjar alltid med siffran <{9}> (nio) vid inloggning\",\n"
                    + "  \"annotatedChallenge\": \"<9><1234567>\",\n"
                    + "  \"links\": {\n"
                    + "    \"next\": {\n"
                    + "      \"method\": \"POST\",\n"
                    + "      \"uri\": \"/v5/identification/securitytoken\"\n"
                    + "    }\n"
                    + "  }\n"
                    + "}";

    public static final String SECURITY_TOKEN_IMAGE =
            "{\n"
                    + "  \"useOneTimePassword\": false,\n"
                    + "  \"challenge\": \"\",\n"
                    + "  \"imageChallenge\": {\n"
                    + "    \"method\": \"GET\",\n"
                    + "    \"uri\": \"/v5/identification/securitytoken/image\"\n"
                    + "  },\n"
                    + "  \"links\": {\n"
                    + "    \"next\": {\n"
                    + "      \"method\": \"POST\",\n"
                    + "      \"uri\": \"/v5/identification/securitytoken\"\n"
                    + "    }\n"
                    + "  }\n"
                    + "}";

    public static final String SECURITY_TOKEN_NO_CHALLENGE_PRESENT =
            "{\n"
                    + "  \"useOneTimePassword\": false,\n"
                    + "  \"challenge\": \"\",\n"
                    + "  \"links\": {\n"
                    + "    \"next\": {\n"
                    + "      \"method\": \"POST\",\n"
                    + "      \"uri\": \"/v5/identification/securitytoken\"\n"
                    + "    }\n"
                    + "  }\n"
                    + "}";
}
