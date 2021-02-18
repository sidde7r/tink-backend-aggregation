package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreditAgricoleTestFixtures {

    public static final String CLIENT_ID = "DUMMY_CLIENT_ID";
    public static final String ACCESS_TOKEN = "DUMMY_ACCESS_TOKEN";
    public static final String REFRESH_TOKEN = "DUMMY_REFRESH_TOKEN";
    public static final String REDIRECT_URL = "http://redirect-url";
    public static final String BASE_URL = "http://base-url";
    public static final String AUTH_CODE = "DUMMY_AUTH_CODE";
    public static final String PSU_IP_ADDR = "127.0.0.1";
    public static final String ACCOUNT_ID = "DUMMY_ACCOUNT_ID";
    public static final String STRING_DATE = "2020-05-28";
    public static final LocalDate DATE = LocalDate.parse(STRING_DATE);

    public static TokenResponse createTokenResponse() {
        final TokenResponse tokenResponse = new TokenResponse();

        tokenResponse.setAccessToken(ACCESS_TOKEN);
        tokenResponse.setRefreshToken(REFRESH_TOKEN);
        tokenResponse.setExpiresIn(3600L);

        return tokenResponse;
    }

    public static GetTransactionsResponse createTransactionsResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"_links\": {\n"
                        + "        \"self\": {\n"
                        + "            \"href\": \"/accounts/123/transactions\",\n"
                        + "            \"templated\": true\n"
                        + "        },\n"
                        + "        \"balances\": {\n"
                        + "            \"href\": \"/accounts/123/balances\",\n"
                        + "            \"templated\": false\n"
                        + "        },\n"
                        + "        \"parent-list\": {\n"
                        + "            \"href\": \"/accounts\",\n"
                        + "            \"templated\": false\n"
                        + "        },\n"
                        + "        \"prev\": null,\n"
                        + "        \"last\": null,\n"
                        + "        \"next\": {\n"
                        + "            \"href\": null,\n"
                        + "            \"templated\": true\n"
                        + "        },\n"
                        + "        \"first\": null\n"
                        + "    },\n"
                        + "    \"transactions\": [\n"
                        + "        {\n"
                        + "            \"resourceId\": null,\n"
                        + "            \"entryReference\": \"1234\",\n"
                        + "            \"bookingDate\": \"2020-01-17\",\n"
                        + "            \"valueDate\": \"2020-01-17\",\n"
                        + "            \"transactionDate\": null,\n"
                        + "            \"status\": \"BOOK\",\n"
                        + "            \"creditDebitIndicator\": \"DBIT\",\n"
                        + "            \"remittanceInformation\": [\n"
                        + "                \"PAIEMENT PAR CARTE PXP*elptoo.fr + 11/01\"\n"
                        + "            ],\n"
                        + "            \"transactionAmount\": {\n"
                        + "                \"currency\": \"EUR\",\n"
                        + "                \"amount\": \"39.00\"\n"
                        + "            }\n"
                        + "        },\n"
                        + "        {\n"
                        + "            \"resourceId\": null,\n"
                        + "            \"entryReference\": \"546645\",\n"
                        + "            \"bookingDate\": \"2020-01-17\",\n"
                        + "            \"valueDate\": \"2020-01-17\",\n"
                        + "            \"transactionDate\": null,\n"
                        + "            \"status\": \"BOOK\",\n"
                        + "            \"creditDebitIndicator\": \"DBIT\",\n"
                        + "            \"remittanceInformation\": [\n"
                        + "                \"PAIEMENT PAR CARTE SARL LMCG LE MANS 11 \"\n"
                        + "            ],\n"
                        + "            \"transactionAmount\": {\n"
                        + "                \"currency\": \"EUR\",\n"
                        + "                \"amount\": \"13.00\"\n"
                        + "            }\n"
                        + "        }\n"
                        + "    ]\n"
                        + "}",
                GetTransactionsResponse.class);
    }
}
