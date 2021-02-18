package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.junit.Ignore;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.TrustedBeneficiariesResponseDto;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FrTransferDestinationFetcherTestFixtures {

    public static final String CURRENCY = "EUR";
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

    public static List<Account> createAccounts() {
        return Arrays.asList(createAccount1(), createAccount2());
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
