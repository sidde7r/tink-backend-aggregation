package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama;

import java.util.Collections;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.junit.Ignore;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher.transfer.dto.TrustedBeneficiariesResponseDto;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.serialization.utils.SerializationUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Ignore
public final class BoursoramaTestFixtures {

    private static final String CURRENCY = "EUR";
    private static final String BENEFICIARY_IBAN = "FR7612548029981234567890161";
    private static final String BENEFICIARY_NAME = "DUMMY_BENEFICIARY_1";
    private static final String BENEFICIARY_BANK = "AXUBFRPP";

    public static TrustedBeneficiariesResponseDto createGetTrustedBeneficiariesResponse() {
        return SerializationUtils.deserializeFromString(
                createGetTrustedBeneficiariesResponseJsonString(),
                TrustedBeneficiariesResponseDto.class);
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
                + "   ]"
                + "}";
    }
}
