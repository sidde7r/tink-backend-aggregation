package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.accountidentifierhandler;

import com.google.common.collect.ImmutableList;
import java.util.List;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.iban.IbanConverter;

public class SparbankenSydSdcAccountIdentifierHandler implements SdcAccountIdentifierHandler {
    private static final String CLEARING_NUMBER = "9570";
    private static final String BANK_CODE = "957";
    private static final int DIGITS_IN_ACCOUNT_NO = 10;
    private static final int DIGITS_OF_ACCOUNT_NO_IN_BBAN = 17;

    private static final String EXCEPTION_MSG = "Given account number has illegal format: ";

    @Override
    public String convertToIban(final String accountNumber) {
        final String bban = convertToBban(accountNumber);
        return IbanConverter.getIban("SE", bban);
    }

    private String convertToBban(String accountNumber) {
        final String normalizedAccountNumber = normalizeAccountNo(accountNumber);

        if (!containsDigitsOnly(normalizedAccountNumber) || normalizedAccountNumber.length() > 10) {
            throw new IllegalArgumentException(EXCEPTION_MSG + accountNumber);
        }

        final String digitCodeForAccountNumber =
                prefixWithZeros(normalizedAccountNumber, DIGITS_OF_ACCOUNT_NO_IN_BBAN);
        return BANK_CODE + digitCodeForAccountNumber;
    }

    @Override
    public List<AccountIdentifier> getIdentifiers(final String rawAccountNumber) {
        return ImmutableList.of(
                new BbanIdentifier(convertToBban(rawAccountNumber)),
                new IbanIdentifier(convertToIban(rawAccountNumber)),
                new SwedishIdentifier(rawAccountNumber));
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
