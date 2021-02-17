package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.data;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.rpc.TokenResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class ChebancaAuthenticatorTestData {
    public static final String ACCESS_TOKEN = "db65aa52-b500-4217-9840-b432270dffff";
    public static final String REFRESH_TOKEN = "db65aa52-b500-4217-9840-b432270dffff";
    public static final String TOKEN_TYPE = "Bearer";
    public static final long ACCESS_TOKEN_EXPIRES_IN = 3501;
    public static final long REFRESH_TOKEN_EXPIRES_IN = 3500;

    public static TokenResponse getTokenResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "   \"result\":{\n"
                        + "      \"messages\":[\n"
                        + "\n"
                        + "      ],\n"
                        + "      \"flushMessages\":true,\n"
                        + "      \"outcome\":\"SUCCESS\",\n"
                        + "      \"requestId\":\"405246309747107896420777\"\n"
                        + "   },\n"
                        + "   \"data\":{\n"
                        + "      \"access_token\":\""
                        + ACCESS_TOKEN
                        + "\",\n"
                        + "      \"token_type\":\""
                        + TOKEN_TYPE
                        + "\",\n"
                        + "      \"expires_in\":"
                        + ACCESS_TOKEN_EXPIRES_IN
                        + ",\n"
                        + "      \"refresh_token\":\""
                        + REFRESH_TOKEN
                        + "\",\n"
                        + "      \"rt_expires_in\":"
                        + REFRESH_TOKEN_EXPIRES_IN
                        + ",\n"
                        + "      \"scope\":\"oob\"\n"
                        + "   }\n"
                        + "}",
                TokenResponse.class);
    }

    public static TokenResponse getTokenResponseWithoutDataEntity() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "   \"result\":{\n"
                        + "      \"messages\":[\n"
                        + "\n"
                        + "      ],\n"
                        + "      \"flushMessages\":true,\n"
                        + "      \"outcome\":\"FAIL\",\n"
                        + "      \"requestId\":\"840461466357638228261818\"\n"
                        + "   }\n"
                        + "}",
                TokenResponse.class);
    }
}
