package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.converter;

import se.tink.libraries.iban.IbanConverter;

public class SparbankenSydAccountNumberToIbanConverter implements AccountNumberToIbanConverter {
    private static final String CLEARING_NUMBER = "9570";
    private static final String BANK_CODE = "957";
    private static final int DIGITS_IN_ACCOUNT_NO = 10;
    private static final int DIGITS_OF_ACCOUNT_NO_IN_BBAN = 17;

    private static final String EXCEPTION_MSG = "Given account number has illegal format: ";

    @Override
    public String convertToIban(final String accountNumber) {
        final String normalizedAccountNumber = normalizeAccountNo(accountNumber);

        if (!containsDigitsOnly(normalizedAccountNumber) || normalizedAccountNumber.length() > 10) {
            throw new IllegalArgumentException(EXCEPTION_MSG + accountNumber);
        }

        final String digitCodeForAccountNumber =
                prefixWithZeros(normalizedAccountNumber, DIGITS_OF_ACCOUNT_NO_IN_BBAN);
        final String bban = BANK_CODE + digitCodeForAccountNumber;
        return IbanConverter.getIban("SE", bban);
    }

    private String normalizeAccountNo(final String accountNumber) {
        return prefixWithZeros(trimClearing(extractAccountNo(accountNumber)), DIGITS_IN_ACCOUNT_NO);
    }

    private String extractAccountNo(final String accountNumber) {
        final String[] parts = accountNumber.split("\\.");
        if (parts.length <= 2) {
            return parts[parts.length - 1];

        } else {
            throw new IllegalArgumentException(EXCEPTION_MSG + accountNumber);
        }
    }

    private String trimClearing(final String accountNumber) {
        if (accountNumber.startsWith(CLEARING_NUMBER)) {
            return accountNumber.substring(CLEARING_NUMBER.length());
        } else {
            return accountNumber;
        }
    }
}
