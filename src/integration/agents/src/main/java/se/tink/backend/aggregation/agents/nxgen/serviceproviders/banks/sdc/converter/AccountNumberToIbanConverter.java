package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.converter;

import org.apache.commons.lang3.StringUtils;

public interface AccountNumberToIbanConverter {
    String convertToIban(final String accountNumber);

    default String prefixWithZeros(final String accountNumber, final int numOfChars) {
        return StringUtils.leftPad(accountNumber, numOfChars, "0");
    }

    default boolean containsDigitsOnly(final String accountNumber) {
        return StringUtils.isNumeric(accountNumber);
    }
}
