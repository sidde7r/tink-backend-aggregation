package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.authenticator.data;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.authenticator.rpc.TokenResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class NorwegianAuthenticatorTestData {
    public static final String ACCESS_TOKEN = "db65aa52-b500-4217-9840-b432270dffff";
    public static final String REFRESH_TOKEN = "db65aa52-b500-4217-9840-b432270dffff";
    public static final String TOKEN_TYPE = "Bearer";
    public static final long ACCESS_TOKEN_EXPIRES_IN = 3501;
    public static final long REFRESH_TOKEN_EXPIRES_IN = 3500;

    public static TokenResponse getTokenResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
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
                        + "      \"scope\":\"readaccounts initiatepayment offline_access\"\n"
                        + "   }",
                TokenResponse.class);
    }

    public static TokenResponse getInvalidTokenResponse() {
        return SerializationUtils.deserializeFromString("{}", TokenResponse.class);
    }
}
