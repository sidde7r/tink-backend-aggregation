package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.junit.Ignore;

import java.net.URI;

@Ignore
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CmcicTestFixtures {

    public static final String SIGNATURE = "DUMMY_SIGNATURE";
    public static final String DIGEST = "DUMMY_DIGEST";
    public static final String KEY_ID = "DUMMY_KEY_ID";
    public static final String REQUEST_ID = "DUMMY_REQUEST_ID";
    public static final String DATE = "2020-05-01";
    public static final String IBAN = "FR6720041010050008697430710";
    public static final String NAME = "DUMMY_NAME";
    public static final String RESOURCE_ID = "DUMMY_RESOURCE_ID";
    public static final String AMOUNT_1 = "123.45";
    public static final String AMOUNT_2 = "345.12";
    public static final String ACCESS_TOKEN = "1234";
    public static final String REFRESH_TOKEN = "4321";
    public static final String TOKEN_TYPE = "bearer";
    public static final String CLIENT_ID = "cid";
    public static final long TOKEN_EXPIRES_IN = 3600L;

    private static final String HOST = "server-url";
    private static final String PATH = "/accounts";
    public static final URI SERVER_URI = URI.create("https://" + HOST + PATH);

    public static final String EXPECTED_GET_SIGNATURE_HEADER_VALUE =
            String.format(
                    "keyId=%s,algorithm=\"rsa-sha256\",headers=\"(request-target) host date x-request-id\",signature=\"%s\"",
                    KEY_ID, SIGNATURE);
    public static final String EXPECTED_POST_SIGNATURE_HEADER_VALUE =
            String.format(
                    "keyId=%s,algorithm=\"rsa-sha256\",headers=\"(request-target) host date x-request-id digest content-type\",signature=\"%s\"",
                    KEY_ID, SIGNATURE);
    public static final String EXPECTED_GET_STRING_TO_SIGN =
            String.format(
                    "(request-target): get %s\nhost: %s\ndate: %s\nx-request-id: %s",
                    PATH, HOST, DATE, REQUEST_ID);
    public static final String EXPECTED_POST_STRING_TO_SIGN =
            String.format(
                    "(request-target): post %s\nhost: %s\ndate: %s\nx-request-id: %s\ndigest: %s\ncontent-type: application/json",
                    PATH, HOST, DATE, REQUEST_ID, DIGEST);
}
