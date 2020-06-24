package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama;

import java.util.Collections;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher.transfer.dto.TrustedBeneficiariesResponseDto;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.serialization.utils.SerializationUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BoursoramaTestFixtures {

    private static final String IBAN_1 = "FR7630006000011234567890189";
    private static final String IBAN_2 = "FR7612345987650123456789014";
    private static final String NAME_1 = "DUMMY_NAME";
    private static final String NAME_2 = "DUMMY_NAME";
    private static final String CURRENCY = "EUR";
    private static final String BENEFICIARY_IBAN = "FR7612548029981234567890161";
    private static final String BENEFICIARY_NAME = "DUMMY_BENEFICIARY_1";
    private static final String BENEFICIARY_BANK = "AXUBFRPP";

    public static TrustedBeneficiariesResponseDto createGetTrustedBeneficiariesResponse() {
        return SerializationUtils.deserializeFromString(
                createGetTrustedBeneficiariesResponseJsonString(),
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

    public static Account createBeneficiaryAccount() {
        final Account account = new Account();

        account.setIdentifiers(Collections.singleton(new IbanIdentifier(BENEFICIARY_IBAN)));
        account.setName(BENEFICIARY_NAME);
        account.setBankId(BENEFICIARY_IBAN);
        account.setCurrencyCode(CURRENCY);

        return account;
    }

    private static String createGetTrustedBeneficiariesResponseJsonString() {
        return "{"
                + "   \"beneficiaries\":["
                + "       {"
                + "           \"id\":null,"
                + "           \"isTrusted\":true,"
                + "           \"creditorAccount\":{"
                + "               \"iban\":\""
                + BENEFICIARY_IBAN
                + "\""
                + "           },"
                + "           \"creditorAgent\":{"
                + "               \"bicFi\":\""
                + BENEFICIARY_BANK
                + "\""
                + "           },"
                + "           \"creditor\":{"
                + "               \"name\":\""
                + BENEFICIARY_NAME
                + "\","
                + "               \"postalAddress\":{"
                + "                     \"country\": \"FR\","
                + "                     \"addressLine\": ["
                + "                         \"18 rue de la DSP2\","
                + "                         \"75008 PARIS\""
                + "                     ]"
                + "                 }"
                + "           }"
                + "       }"
                + "   ],"
                + "   \"_links\":{"
                + "       \"self\":{"
                + "           \"href\": \"/_user_/_{userHash}_/domain/controller/service/{resourceId}\","
                + "           \"method\": \"GET\""
                + "       }"
                + "   }"
                + "}";
    }
}
