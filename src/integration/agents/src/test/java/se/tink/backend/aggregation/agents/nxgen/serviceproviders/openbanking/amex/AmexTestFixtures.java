package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmericanExpressConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.AccountsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.BalanceDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.TransactionsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.token.RevokeResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.token.TokenResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount.storage.HmacAccountIds;
import se.tink.backend.aggregation.nxgen.core.authentication.HmacMultiToken;
import se.tink.backend.aggregation.nxgen.core.authentication.HmacToken;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class AmexTestFixtures {

    public static final String ACCESS_TOKEN_1 = "3f705eb0-f432-4211-aa7c-c16b8cb9f39f";
    public static final String ACCESS_TOKEN_2 = "59d598e3-9d21-4597-b554-d9a15898a094";
    public static final String MAC_KEY = "ea7400c9-ce13-481b-902a-1a60e97bf683";
    public static final String CLIENT_ID = "1234";
    public static final String CLIENT_SECRET = "secret";
    public static final String AUTH_MAC_VALUE = "MAC345";
    public static final String DATA_MAC_VALUE = "MAC923";
    public static final String APP_ID = "appId";
    public static final String CLUSTER_ID = "clusterId";
    public static final String MAC_SIGNATURE = "signature";
    public static final String NONCE = "abd9b15f-2227-4218-8cbc-9376ed57496b:AMEX";
    private static final long TIMESTAMP = 1583331760L;
    public static final long MILLIS = TIMESTAMP * 1000L;
    public static final String RESOURCE_PATH = "/resource/path";
    private static final String SERVER_HOST = Urls.SERVER_URL.toUri().getHost();
    public static final String SERVER_URL = Urls.SERVER_URL.get();
    public static final String AUTHORIZE_URL = Urls.GRANT_ACCESS_JOURNEY_URL.get();
    public static final String REDIRECT_URL = "http://redirect-url";
    public static final String AUTH_CODE_1 = "456";
    public static final String AUTH_CODE_2 = "789";
    public static final String VALID_REFRESH_TOKEN = "9835d74e-831c-4639-bf92-f873ef0fc551";
    public static final String EXPIRED_REFRESH_TOKEN = "68aa65df-5d9f-4919-9a97-a443e473355e";
    public static final String ACCOUNT_NUMBER_1 = "XXXX-XXXXXX-81007";
    public static final String ACCOUNT_NUMBER_2 = "XXXX-XXXXXX-82009";
    public static final String ORIGINATING_USER_IP = "127.0.0.1";
    public static final String STATEMENT_MAP = "{\"0\":\"2000-01-01\"}";

    public static HmacMultiToken createMultiTokenWithExpiredAccessToken() {
        final HmacToken validHmacToken =
                createHmacToken(VALID_REFRESH_TOKEN, 100_000L, ACCESS_TOKEN_1);
        final HmacToken expiredHmacToken =
                createHmacToken(EXPIRED_REFRESH_TOKEN, 0L, ACCESS_TOKEN_2);

        return createMultiToken(Arrays.asList(validHmacToken, expiredHmacToken));
    }

    public static HmacMultiToken createMultiTokenWithValidAccessToken() {
        final HmacToken validHmacToken1 =
                createHmacToken(VALID_REFRESH_TOKEN, 100_000L, ACCESS_TOKEN_1);
        final HmacToken validHmacToken2 =
                createHmacToken(EXPIRED_REFRESH_TOKEN, 100_000L, ACCESS_TOKEN_2);

        return createMultiToken(Arrays.asList(validHmacToken1, validHmacToken2));
    }

    public static String createAccountsResponseJsonString(String accountNumber) {
        return "{\n"
                + "   \"identifiers\":{\n"
                + "      \"display_account_number\": \""
                + accountNumber
                + "\",\n"
                + "      \"is_basic\":true,\n"
                + "      \"account_key\":\"123456\",\n"
                + "      \"supplementary_account_count\":\"0\"\n"
                + "   },\n"
                + "   \"holder\":{\n"
                + "      \"profile\":{\n"
                + "         \"first_name\":\"Holdername\",\n"
                + "         \"last_name\":\"Lastname\",\n"
                + "         \"embossed_name\":\"Holdername Lastname\"\n"
                + "      },\n"
                + "      \"localization_preferences\":{\n"
                + "         \"localization_id\":\"sv-SE-revolve\",\n"
                + "         \"home_country_locale\":\"sv-SE\",\n"
                + "         \"currency_locale\":\"sv-SE\",\n"
                + "         \"date_locale\":\"sv-SE\",\n"
                + "         \"language_preference\":\"sv-SE\",\n"
                + "         \"language_preference_code\":\"SV\",\n"
                + "         \"geo_country_locale\":\"sv-SE\"\n"
                + "      }\n"
                + "   },\n"
                + "   \"product\":{\n"
                + "      \"digital_info\":{\n"
                + "         \"product_desc\":\"SAS Amex Classic\",\n"
                + "         \"digital_asset_url\":\"https://secure.cmax.americanexpress.com/Internet/CardArt/EMEA/se-cardasset-config/images/GSEGREN0SA01.jpg\"\n"
                + "      },\n"
                + "      \"account_types\":{\n"
                + "         \"plastic_types\":[\n"
                + "            \"Green\",\n"
                + "            \"AffinityCard\",\n"
                + "            \"CoBrandCard\"\n"
                + "         ],\n"
                + "         \"line_of_business_type\":\"ConsumerCard\",\n"
                + "         \"payment_type\":\"Revolve\"\n"
                + "      },\n"
                + "      \"account_eligibilities\":[\n"
                + "         \"IntlAllEstmServiceEligible\",\n"
                + "         \"MembershipRewardsEligible\",\n"
                + "         \"ProdIDInfoAvailableEligible\"\n"
                + "      ],\n"
                + "      \"account_features\":{\n"
                + "         \n"
                + "      },\n"
                + "      \"line_of_business\":{\n"
                + "         \n"
                + "      }\n"
                + "   },\n"
                + "   \"status\":{\n"
                + "      \"account_status\":[\n"
                + "         \"Active\"\n"
                + "      ]\n"
                + "   }\n"
                + "}";
    }

    public static AccountsResponseDto createAccountsResponse() {
        return SerializationUtils.deserializeFromString(
                createAccountsResponseJsonString(ACCOUNT_NUMBER_1), AccountsResponseDto.class);
    }

    @SuppressWarnings("unchecked")
    public static List<LinkedHashMap<String, String>> createBalancesRawResponse() {
        return Collections.singletonList(
                SerializationUtils.deserializeFromString(
                        getBalancesJsonString(), LinkedHashMap.class));
    }

    @SuppressWarnings("unchecked")
    public static List<LinkedHashMap<String, String>> createTransactionsRawResponse() {
        return Collections.singletonList(
                SerializationUtils.deserializeFromString(
                        getTransactionJsonString(), LinkedHashMap.class));
    }

    public static List<BalanceDto> createBalancesResponse() {
        return Collections.singletonList(createBalanceDto());
    }

    public static String createBalancesResponseJsonString() {
        return String.format("[\n%s\n]", getBalancesJsonString());
    }

    public static String createStatementPeriodsResponseJsonString() {
        return getStatementPeriodJsonString();
    }

    public static List<TransactionsResponseDto> createTransactionsResponse() {
        return Collections.singletonList(createTransactionsResponseDto());
    }

    public static String createTransactionsResponseJsonString() {
        return String.format("[\n%s\n]", getTransactionJsonString());
    }

    public static HmacToken createHmacToken() {
        return new HmacToken(
                HmacToken.MAC_TOKEN_TYPE, ACCESS_TOKEN_1, VALID_REFRESH_TOKEN, MAC_KEY, 1_000_000L);
    }

    public static HmacAccountIds createHmacAccountIds(HmacToken hmacToken) {
        final Map<String, HmacToken> tokensByAccountId = new HashMap<>();
        tokensByAccountId.put(ACCOUNT_NUMBER_1, hmacToken);

        return new HmacAccountIds(tokensByAccountId);
    }

    public static HmacMultiToken createMultiToken(List<HmacToken> tokens) {
        return new HmacMultiToken(tokens);
    }

    public static HmacMultiToken createMultiToken(HmacToken hmacToken) {
        return createMultiToken(Collections.singletonList(hmacToken));
    }

    public static HmacToken createHmacToken(String refreshToken, long expiresInSeconds) {
        return createHmacToken(refreshToken, expiresInSeconds, ACCESS_TOKEN_1);
    }

    public static String createAuthMacValue() {
        return String.format(
                "MAC id=\"%s\",ts=\"%d\",nonce=\"%s\",mac=\"%s\"",
                CLIENT_ID, TIMESTAMP, NONCE, MAC_SIGNATURE);
    }

    public static String createDataMacValue() {
        return String.format(
                "MAC id=\"%s\",ts=\"%s\",nonce=\"%s\",mac=\"%s\"",
                ACCESS_TOKEN_1, TIMESTAMP, NONCE, MAC_SIGNATURE);
    }

    public static String createBaseAuthString() {
        return String.format(
                "%s\n%s\n%s\n%s\n",
                CLIENT_ID, TIMESTAMP, NONCE, AmexGrantType.AUTHORIZATION_CODE.getType());
    }

    public static String createBaseDataString() {
        return String.format(
                "%s\n%s\nGET\n%s\n%s\n443\n\n",
                TIMESTAMP, NONCE, URL.urlEncode(RESOURCE_PATH), SERVER_HOST);
    }

    public static String createRetrieveAccessTokenRequestBody(String accessCode) {
        final String scopeList = "MEMBER_ACCT_INFO+FINS_STP_DTLS+FINS_BAL_INFO+FINS_TXN_INFO";

        return String.format(
                "scope=%s&redirect_uri=%s&grant_type=authorization_code&code=%s",
                scopeList, URL.urlEncode(REDIRECT_URL), accessCode);
    }

    public static String createRefreshAccessTokenRequestBody(String refreshToken) {
        return String.format("grant_type=refresh_token&refresh_token=%s", refreshToken);
    }

    public static String createAccessTokenResponseJsonString(String accessToken) {
        return "{\n"
                + "  \"access_token\":\""
                + accessToken
                + "\",\n"
                + "  \"token_type\":\"mac\",\n"
                + "  \"expires_in\":604800,\n"
                + "  \"refresh_token\":\""
                + VALID_REFRESH_TOKEN
                + "\",\n"
                + "  \"scope\":\"MEMBER_ACCT_INFO FINS_STP_DTLS FINS_BAL_INFO FINS_TXN_INFO\",\n"
                + "  \"mac_key\":\""
                + MAC_KEY
                + "\",\n"
                + "  \"mac_algorithm\":\"hmac-sha-256\"\n"
                + "}";
    }

    public static TokenResponseDto createAccessTokenResponse() {
        return SerializationUtils.deserializeFromString(
                createAccessTokenResponseJsonString(ACCESS_TOKEN_1), TokenResponseDto.class);
    }

    public static RevokeResponseDto createRevokeTokenResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"result\":\"success\",\n"
                        + "  \"expires_in\":604800,\n"
                        + "  \"revoked_tokens\":[\n"
                        + "\""
                        + ACCESS_TOKEN_1
                        + "\"\n"
                        + "],\n"
                        + "  \"invalid_tokens\":[\n"
                        + "]\n"
                        + "}",
                RevokeResponseDto.class);
    }

    public static String createAccessTokenRevokedErrorResponse() {
        return "{"
                + "\"code\" : \"154011\","
                + " \"message\": \"[ERR_OAS_0002] Access Token revoked by user\""
                + "}";
    }

    public static String getTokenRequest() {
        try {
            return String.format(
                    "scope=%s&redirect_uri=%s&grant_type=authorization_code&code=%s",
                    "MEMBER_ACCT_INFO+FINS_STP_DTLS+FINS_BAL_INFO+FINS_TXN_INFO",
                    URLEncoder.encode(REDIRECT_URL, StandardCharsets.UTF_8.toString()),
                    AUTH_CODE_1);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String getRefreshRequest() {
        return String.format("grant_type=refresh_token&refresh_token=%s", VALID_REFRESH_TOKEN);
    }

    public static String getRevokeRequest() {
        return String.format(
                "grant_type=revoke&request_type=single&access_token=%s", ACCESS_TOKEN_1);
    }

    public static String getAuthorizeUrl(String state) {
        final String scopeList = "MEMBER_ACCT_INFO,FINS_STP_DTLS,FINS_BAL_INFO,FINS_TXN_INFO";

        return String.format(
                "%s?redirect_uri=%s&client_id=%s&scope_list=%s&state=%s",
                AUTHORIZE_URL,
                URL.urlEncode(REDIRECT_URL),
                CLIENT_ID,
                URL.urlEncode(scopeList),
                state);
    }

    private static HmacToken createHmacToken(
            String refreshToken, long expiresInSeconds, String accessToken) {
        return new HmacToken(
                HmacToken.MAC_TOKEN_TYPE, accessToken, refreshToken, MAC_KEY, expiresInSeconds);
    }

    private static BalanceDto createBalanceDto() {
        return SerializationUtils.deserializeFromString(getBalancesJsonString(), BalanceDto.class);
    }

    private static TransactionsResponseDto createTransactionsResponseDto() {
        return SerializationUtils.deserializeFromString(
                getTransactionJsonString(), TransactionsResponseDto.class);
    }

    private static String getBalancesJsonString() {
        return "  {\n"
                + "    \"last_statement_balance_amount\": \"55000.00\",\n"
                + "    \"debits_balance_amount\": \"0.00\",\n"
                + "    \"payments_credits_amount\": \"0.00\",\n"
                + "    \"statement_balance_amount\": \"55000.00\",\n"
                + "    \"payment_due_amount\": \"55000.00\",\n"
                + "    \"iso_alpha_currency_code\": \"GBP\",\n"
                + "    \"payment_due_date\": \"2014-02-15\",\n"
                + "    \"remaining_statement_balance_amount\": \"55000.00\"\n"
                + "  }";
    }

    private static String getStatementPeriodJsonString() {
        return "  {\n"
                + "   \"statement_periods\":[\n"
                + "      {\n"
                + "         \"start_date\":\"2021-02-03\",\n"
                + "         \"end_date\":\"2021-03-02\",\n"
                + "         \"index\":0\n"
                + "      }\n"
                + "   ]\n"
                + "}";
    }

    private static String getTransactionJsonString() {
        return "  {\n"
                + "    \"total_transaction_count\": \"1\",\n"
                + "    \"transactions\": [\n"
                + "      {\n"
                + "        \"identifier\": \"320162240910370230\",\n"
                + "        \"charge_date\": \"2016-08-10\",\n"
                + "        \"post_date\": \"2016-08-10\",\n"
                + "        \"statement_end_date\": \"2016-09-06\",\n"
                + "        \"amount\": \"10\",\n"
                + "        \"reference_number\": \"320162240910370230\",\n"
                + "        \"type\": \"DEBIT\",\n"
                + "        \"description\": \"NIGEL'S BAGEL EMPORIUM 194 0194\",\n"
                + "        \"iso_alpha_currency_code\": \"GBP\",\n"
                + "        \"display_account_number\": \"XXXX-XXXXXX-81004\",\n"
                + "        \"first_name\": \"Nigel\",\n"
                + "        \"last_name\": \"Smythe\",\n"
                + "        \"embossed_name\": \"Nigel Smythe\",\n"
                + "        \"extended_details\": {\n"
                + "          \"merchant\": {\n"
                + "            \"name\": \"NIGEL'S BAGEL EMPORIUM\",\n"
                + "            \"address\": {\n"
                + "              \"address_lines\": [\n"
                + "                \"18631 N 19TH AVE, 150\"\n"
                + "              ]\n"
                + "            }\n"
                + "          }\n"
                + "        }\n"
                + "      }\n"
                + "    ]\n"
                + "  }";
    }

    public static String createTransactionRangeErrorResponse() {
        return "{\"code\":3027,\"message\":\"Request Validation Failed - Requested date range exceeds the supported limit\"}\n";
    }
}
