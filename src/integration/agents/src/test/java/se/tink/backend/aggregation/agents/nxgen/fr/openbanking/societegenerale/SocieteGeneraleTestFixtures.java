package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale;

import java.util.Collections;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transfer.rpc.TrustedBeneficiariesResponse;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.serialization.utils.SerializationUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SocieteGeneraleTestFixtures {

    public static final String SIGNATURE = "DUMMY_SIGNATURE";
    public static final String ACCESS_TOKEN = "DUMMY_ACCESS_TOKEN";
    public static final String BEARER_HEADER_VALUE = "Bearer " + ACCESS_TOKEN;
    public static final String CLIENT_ID = "DUMMY_CLIENT_ID";
    public static final String NEXT_PAGE_PATH = "/trusted-beneficiaries?page=2";
    public static final String REDIRECT_URL = "https://127.0.0.1:7357/api/v1/thirdparty/callback";
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

    public static TrustedBeneficiariesResponse createTrustedBeneficiariesPage1Response() {
        return SerializationUtils.deserializeFromString(
                createTrustedBeneficiariesResponsePage1JsonString(),
                TrustedBeneficiariesResponse.class);
    }

    public static TrustedBeneficiariesResponse createTrustedBeneficiariesPage2Response() {
        return SerializationUtils.deserializeFromString(
                createTrustedBeneficiariesResponsePage2JsonString(),
                TrustedBeneficiariesResponse.class);
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

    private static String createTrustedBeneficiariesResponsePage1JsonString() {
        return "{\n"
                + "  \"beneficiaries\": [\n"
                + "    {\n"
                + "      \"creditorAgent\": {\n"
                + "        \"bicFi\": \""
                + BENEFICIARY_BANK
                + "\"\n"
                + "      },\n"
                + "      \"creditor\": {\n"
                + "        \"name\": \""
                + BENEFICIARY_NAME_1
                + "\",\n"
                + "        \"postalAddress\": {\n"
                + "          \"country\": \"FR\",\n"
                + "          \"addressLine\": [\n"
                + "            \"79 rue DES TPPL\",\n"
                + "            \"75019 PARIS\"\n"
                + "          ]\n"
                + "        }\n"
                + "      },\n"
                + "      \"creditorAccount\": {\n"
                + "        \"iban\": \""
                + BENEFICIARY_IBAN_1
                + "\"\n"
                + "      }\n"
                + "    }\n"
                + "  ],\n"
                + "  \"_links\": {\n"
                + "    \"self\": {\n"
                + "      \"href\": \"/trusted-beneficiaries?page=1\"\n"
                + "    },\n"
                + "    \"parent-list\": {\n"
                + "      \"href\": \"/accounts\"\n"
                + "    },\n"
                + "    \"first\": {\n"
                + "      \"href\": \"/trusted-beneficiaries?page=1\"\n"
                + "    },\n"
                + "    \"last\": {\n"
                + "      \"href\": \"/trusted-beneficiaries?page=2\"\n"
                + "    },\n"
                + "    \"next\": {\n"
                + "      \"href\": \""
                + NEXT_PAGE_PATH
                + "\"\n"
                + "    }\n"
                + "  }\n"
                + "}";
    }

    private static String createTrustedBeneficiariesResponsePage2JsonString() {
        return "{\n"
                + "  \"beneficiaries\": [\n"
                + "    {\n"
                + "      \"creditorAgent\": {\n"
                + "        \"bicFi\": \""
                + BENEFICIARY_BANK
                + "\"\n"
                + "      },\n"
                + "      \"creditor\": {\n"
                + "        \"name\": \""
                + BENEFICIARY_NAME_2
                + "\",\n"
                + "        \"postalAddress\": {\n"
                + "          \"country\": \"FR\",\n"
                + "          \"addressLine\": [\n"
                + "            \"79 rue DES TPPL\",\n"
                + "            \"75019 PARIS\"\n"
                + "          ]\n"
                + "        }\n"
                + "      },\n"
                + "      \"creditorAccount\": {\n"
                + "        \"iban\": \""
                + BENEFICIARY_IBAN_2
                + "\"\n"
                + "      }\n"
                + "    }\n"
                + "  ],\n"
                + "  \"_links\": {\n"
                + "    \"self\": {\n"
                + "      \"href\": \"/trusted-beneficiaries?page=2\"\n"
                + "    },\n"
                + "    \"parent-list\": {\n"
                + "      \"href\": \"/accounts\"\n"
                + "    },\n"
                + "    \"first\": {\n"
                + "      \"href\": \"/trusted-beneficiaries?page=1\"\n"
                + "    },\n"
                + "    \"last\": {\n"
                + "      \"href\": \"/trusted-beneficiaries?page=2\"\n"
                + "    },\n"
                + "    \"prev\": {\n"
                + "      \"href\": \"/trusted-beneficiaries?page=1\"\n"
                + "    }\n"
                + "  }\n"
                + "}";
    }
}
