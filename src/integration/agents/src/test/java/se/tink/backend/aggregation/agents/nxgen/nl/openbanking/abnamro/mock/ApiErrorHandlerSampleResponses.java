package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.mock;

public class ApiErrorHandlerSampleResponses {
    static final String OAUTH2_ERROR_RESPONSE_WITH_INVALID_GRANT =
            "{\"error_description\":\"unknown, invalid, or expired refresh token\",\"error\":\"invalid_grant\"}";
    static final String OAUTH2_ERROR_RESPONSE_WITH_UNKNOWN_ERROR =
            "{\"error_description\":\"unknown, invalid, or expired refresh token\",\"error\":\"unknown_error\"}";

    static final String ERROR_RESPONSE_401 =
            "{"
                    + "           \"errors\": [\n"
                    + "                {\n"
                    + "                    \"code\": \"ERR_2100_001\",\n"
                    + "                    \"message\": \"Invalid or expired access token\",\n"
                    + "                    \"reference\": \"https://developer.abnamro.com\",\n"
                    + "                    \"traceId\": \"4052e116-cf19-4b26-84d9-782d8f49cfa7\",\n"
                    + "                    \"status\": 401,\n"
                    + "                    \"category\":\"BACKEND_ERROR\"\n"
                    + "                }\n"
                    + "               ]"
                    + "            }";

    static final String ERROR_RESPONSE_503 =
            "{"
                    + "           \"errors\": [\n"
                    + "                {\n"
                    + "                     \"code\": \"ERR_9001_005\",\n"
                    + "                     \"message\": \"Service is currently unavailable. Please try after some time.\",\n"
                    + "                     \"reference\": \"https://developer.abnamro.com/api-products/account-information-psd2/reference-documentation\",\n"
                    + "                     \"traceId\": \"7df59bd4-8991-4dbb-8004-edbcca4bf35b\",\n"
                    + "                     \"status\": 503,\n"
                    + "                     \"category\":\"SERVICE_UNAVAILABLE\"\n"
                    + "                }\n"
                    + "               ]\n"
                    + "            }";

    static final String ERROR_RESPONSE_429 =
            "{"
                    + "           \"errors\": [\n"
                    + "                {\n"
                    + "                     \"code\": \"ERR_7002_001\",\n"
                    + "                     \"message\": \"Rate limit violation\",\n"
                    + "                     \"reference\": \"https://developer.abnamro.com/api-products/account-information-psd2/reference-documentation\",\n"
                    + "                     \"traceId\": \"7df59bd4-8991-4dbb-8004-edbcca4bf35b\",\n"
                    + "                     \"status\": 429,\n"
                    + "                     \"category\":\"TOO_MANY_REQUESTS\"\n"
                    + "                }\n"
                    + "               ]\n"
                    + "            }";
}
