package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic;

import java.net.URI;
import java.util.Collections;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transfer.dto.TrustedBeneficiariesResponseDto;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.serialization.utils.SerializationUtils;

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
    public static final String CURRENCY = "EUR";
    public static final String ACCESS_TOKEN = "1234";
    public static final String REFRESH_TOKEN = "4321";
    public static final String TOKEN_TYPE = "bearer";
    public static final String CLIENT_ID = "cid";
    public static final long TOKEN_EXPIRES_IN = 3600L;
    public static final String BENEFICIARIES_2ND_PAGE_PATH = "/trusted-beneficiaries?page=2";

    private static final String IBAN_1 = "FR7630006000011234567890189";
    private static final String IBAN_2 = "FR7612345987650123456789014";
    private static final String NAME_1 = "DUMMY_NAME";
    private static final String NAME_2 = "DUMMY_NAME";
    private static final String BENEFICIARY_IBAN_1 = "FR7612548029981234567890161";
    private static final String BENEFICIARY_IBAN_2 = "FR7610278001110002007680247";
    private static final String BENEFICIARY_NAME_1 = "DUMMY_BENEFICIARY_1";
    private static final String BENEFICIARY_NAME_2 = "DUMMY_BENEFICIARY_2";
    private static final String BENEFICIARY_BANK = "AXUBFRPP";
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

    public static TrustedBeneficiariesResponseDto createTrustedBeneficiariesPage1Response() {
        return SerializationUtils.deserializeFromString(
                createGetTrustedBeneficiariesResponsePage1JsonString(),
                TrustedBeneficiariesResponseDto.class);
    }

    public static TrustedBeneficiariesResponseDto createTrustedBeneficiariesPage2Response() {
        return SerializationUtils.deserializeFromString(
                createGetTrustedBeneficiariesResponsePage2JsonString(),
                TrustedBeneficiariesResponseDto.class);
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

    private static String createGetTrustedBeneficiariesResponsePage1JsonString() {
        return "{"
                + "   \"beneficiaries\":["
                + "       {"
                + "           \"id\":null,"
                + "           \"isTrusted\":true,"
                + "           \"creditorAccount\":{"
                + "               \"iban\":\""
                + BENEFICIARY_IBAN_1
                + "\""
                + "           },"
                + "           \"creditorAgent\":{"
                + "               \"bicFi\":\""
                + BENEFICIARY_BANK
                + "\""
                + "           },"
                + "           \"creditor\":{"
                + "               \"name\":\""
                + BENEFICIARY_NAME_1
                + "\","
                + "               \"postalAddress\":null"
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
                + "\""
                + "           },"
                + "           \"creditorAgent\":{"
                + "               \"bicFi\":\""
                + BENEFICIARY_BANK
                + "\""
                + "           },"
                + "           \"creditor\":{"
                + "               \"name\":\""
                + BENEFICIARY_NAME_2
                + "\","
                + "               \"postalAddress\":null"
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
