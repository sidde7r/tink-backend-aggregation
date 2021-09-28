package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.accountidentifierhandler;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import se.tink.libraries.account.AccountIdentifier;

public interface SdcAccountIdentifierHandler {
    String convertToIban(final String rawAccountNumber);

    List<AccountIdentifier> getIdentifiers(final String rawAccountNumber);

    default String prefixWithZeros(final String accountNumber, final int numOfChars) {
        return StringUtils.leftPad(accountNumber, numOfChars, "0");
    }

    default boolean containsDigitsOnly(final String accountNumber) {
        return StringUtils.isNumeric(accountNumber);
    }

    default String normalizedBankId(String rawAccountNumber) {
        return rawAccountNumber.replace(".", "");
    }
}
