package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole;

import java.time.LocalDate;
import java.util.Collections;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.GetTrustedBeneficiariesResponse;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.serialization.utils.SerializationUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreditAgricoleTestFixtures {

    public static final String CLIENT_ID = "DUMMY_CLIENT_ID";
    public static final String ACCESS_TOKEN = "DUMMY_ACCESS_TOKEN";
    public static final String REFRESH_TOKEN = "DUMMY_REFRESH_TOKEN";
    public static final String REDIRECT_URL = "http://redirect-url";
    public static final String BASE_URL = "http://base-url";
    public static final String AUTH_CODE = "DUMMY_AUTH_CODE";
    public static final String PSU_IP_ADDR = "0.0.0.0";
    public static final String ACCOUNT_ID = "DUMMY_ACCOUNT_ID";
    public static final String STRING_DATE = "2020-05-28";
    public static final LocalDate DATE = LocalDate.parse(STRING_DATE);
    public static final String BENEFICIARIES_2ND_PAGE_PATH = "/trusted-beneficiaries?page=2";
    private static final String IBAN_1 = "FR7630006000011234567890189";
    private static final String IBAN_2 = "FR7612345987650123456789014";
    private static final String NAME_1 = "DUMMY_NAME";
    private static final String NAME_2 = "DUMMY_NAME";
    private static final String CURRENCY = "EUR";
    private static final String BENEFICIARY_IBAN_1 = "FR7612548029981234567890161";
    private static final String BENEFICIARY_IBAN_2 = "FR7610278001110002007680247";
    private static final String BENEFICIARY_NAME_1 = "DUMMY_BENEFICIARY_1";
    private static final String BENEFICIARY_NAME_2 = "DUMMY_BENEFICIARY_2";
    private static final String BENEFICIARY_BANK = "AXUBFRPP";

    public static GetTrustedBeneficiariesResponse createGetTrustedBeneficiariesPage1Response() {
        return SerializationUtils.deserializeFromString(
                createGetTrustedBeneficiariesResponsePage1JsonString(),
                GetTrustedBeneficiariesResponse.class);
    }

    public static GetTrustedBeneficiariesResponse createGetTrustedBeneficiariesPage2Response() {
        return SerializationUtils.deserializeFromString(
                createGetTrustedBeneficiariesResponsePage2JsonString(),
                GetTrustedBeneficiariesResponse.class);
    }

    public static Account createAccount1() {
        final Account account = new Account();

        account.setIdentifiers(Collections.singleton(new IbanIdentifier(IBAN_1)));
        account.setName(NAME_1);
        account.setBankId(IBAN_1);
        account.setCurrencyCode(CURRENCY);

        return account;
    }

    public static Account createAccount2() {
        final Account account = new Account();

        account.setIdentifiers(Collections.singleton(new IbanIdentifier(IBAN_2)));
        account.setName(NAME_2);
        account.setBankId(IBAN_2);
        account.setCurrencyCode(CURRENCY);

        return account;
    }

    public static Account createBeneficiary1Account() {
        final Account account = new Account();

        account.setIdentifiers(Collections.singleton(new IbanIdentifier(BENEFICIARY_IBAN_1)));
        account.setName(BENEFICIARY_NAME_1);
        account.setBankId(BENEFICIARY_IBAN_1);
        account.setCurrencyCode(CURRENCY);

        return account;
    }

    public static Account createBeneficiary2Account() {
        final Account account = new Account();

        account.setIdentifiers(Collections.singleton(new IbanIdentifier(BENEFICIARY_IBAN_2)));
        account.setName(BENEFICIARY_NAME_2);
        account.setBankId(BENEFICIARY_IBAN_2);
        account.setCurrencyCode(CURRENCY);

        return account;
    }

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

    private static String createGetTrustedBeneficiariesResponsePage1JsonString() {
        return "{"
                + "   \"beneficiaries\":["
                + "       {"
                + "           \"id\":null,"
                + "           \"isTrusted\":true,"
                + "           \"creditorAccount\":{"
                + "               \"iban\":\""
                + BENEFICIARY_IBAN_1
                + "\","
                + "               \"other\":null"
                + "           },"
                + "           \"creditorAgent\":{"
                + "               \"bicFi\":\""
                + BENEFICIARY_BANK
                + "\","
                + "               \"name\":null,"
                + "               \"clearingSystemMemberId\":null,"
                + "               \"postalAddress\":null"
                + "           },"
                + "           \"creditor\":{"
                + "               \"name\":\""
                + BENEFICIARY_NAME_1
                + "\","
                + "               \"postalAddress\":null,"
                + "               \"organisationId\":null,"
                + "               \"privateId\":null"
                + "           }"
                + "       }"
                + "   ],"
                + "   \"_links\":{"
                + "       \"self\":{"
                + "           \"href\":\"/trusted-beneficiaries\","
                + "           \"templated\":false"
                + "       },"
                + "       \"parent-list\":null,"
                + "       \"first\":null,"
                + "       \"last\":null,"
                + "       \"next\":{"
                + "           \"href\":\""
                + BENEFICIARIES_2ND_PAGE_PATH
                + "\","
                + "           \"templated\":true"
                + "       },"
                + "       \"prev\":null"
                + "   }"
                + "}";
    }

    private static String createGetTrustedBeneficiariesResponsePage2JsonString() {
        return "{"
                + "   \"beneficiaries\":["
                + "       {"
                + "           \"id\":null,"
                + "           \"isTrusted\":true,"
                + "           \"creditorAccount\":{"
                + "               \"iban\":\""
                + BENEFICIARY_IBAN_2
                + "\","
                + "               \"other\":null"
                + "           },"
                + "           \"creditorAgent\":{"
                + "               \"bicFi\":\""
                + BENEFICIARY_BANK
                + "\","
                + "               \"name\":null,"
                + "               \"clearingSystemMemberId\":null,"
                + "               \"postalAddress\":null"
                + "           },"
                + "           \"creditor\":{"
                + "               \"name\":\""
                + BENEFICIARY_NAME_2
                + "\","
                + "               \"postalAddress\":null,"
                + "               \"organisationId\":null,"
                + "               \"privateId\":null"
                + "           }"
                + "       }"
                + "   ],"
                + "   \"_links\":{"
                + "       \"self\":{"
                + "           \"href\":\"/trusted-beneficiaries\","
                + "           \"templated\":false"
                + "       },"
                + "       \"parent-list\":null,"
                + "       \"first\":null,"
                + "       \"last\":null,"
                + "       \"next\":null,"
                + "       \"prev\":null"
                + "   }"
                + "}";
    }
}
