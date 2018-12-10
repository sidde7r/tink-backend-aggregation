package se.tink.backend.aggregation.agents.banks.danskebank.v2;

import com.google.common.base.Objects;
import se.tink.libraries.account.identifiers.se.swedbank.SwedbankClearingNumberUtils;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.formatters.AccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.se.ClearingNumber;

public class DanskeBankAccountIdentifierFormatter implements AccountIdentifierFormatter {
    private static final DefaultAccountIdentifierFormatter DEFAULT_FORMATTER = new DefaultAccountIdentifierFormatter();

    @Override
    public String apply(AccountIdentifier identifier) {
        if (identifier.is(Type.SE)) {
            return toDanskeDestination(identifier.to(SwedishIdentifier.class));
        }
        return identifier.getIdentifier(DEFAULT_FORMATTER);
    }

    /**
     * Reformats according to the rules Danske has. Rules interpreted from:
     * http://www.danskebank.se/sv-se/privat/kontopaket/betala/betala-rakningar/betala-rakningar-i-hembanken/pages/bankernas-kontonummer-clearingnummer.aspx
     */
    private static String toDanskeDestination(SwedishIdentifier swedishIdentifier) {
        ClearingNumber.Details clearing = getClearingNumberDetails(swedishIdentifier);

        switch (clearing.getBank()) {
        case SWEDBANK:
            return formatSwedbankIdentifier(swedishIdentifier);
        case NORDEA_PERSONKONTO:
            return swedishIdentifier.getAccountNumber(); // Nordea Personkonto should have no clearing
        default:
            return swedishIdentifier.getIdentifier(DEFAULT_FORMATTER);
        }
    }

    /**
     * For 8xxxx Swedbank identifiers Danske removes fifth clearing number digit, but needs padding of account number
     * @param swedishIdentifier Swedbank account identifier
     */
    private static String formatSwedbankIdentifier(SwedishIdentifier swedishIdentifier) {
        if (!swedishIdentifier.getClearingNumber().startsWith("8")) {
            return swedishIdentifier.getIdentifier(DEFAULT_FORMATTER);
        }

        String clearingWithoutFifthDigit = getClearingWithoutFifthDigit(swedishIdentifier.getClearingNumber());
        String paddedAccountNumber = SwedbankClearingNumberUtils
                .padWithZerosBeforeAccountNumber(swedishIdentifier.getAccountNumber());
        return clearingWithoutFifthDigit + paddedAccountNumber;
    }

    /**
     * Danske requires fifth clearing of Swedbank account identifiers to be removed
     */
    private static String getClearingWithoutFifthDigit(String clearingNumber) {
        if (clearingNumber.length() == 5) {
            return clearingNumber.substring(0, 4);
        } else {
            return clearingNumber;
        }
    }

    private static ClearingNumber.Details getClearingNumberDetails(SwedishIdentifier swedishIdentifier) {
        String clearingNumber = swedishIdentifier.getClearingNumber();
        return ClearingNumber.get(clearingNumber).get();
    }

    public SwedishIdentifier parseSwedishIdentifier(String bank, String accountNumber) {
        if (Objects.equal(bank, "NORDEA - PERSONKONTON")) {
            return new SwedishIdentifier("3300" + accountNumber);
        } else if (Objects.equal(bank, "SWEDBANK")) {
            String completeAccountNumber = parseCompleteSwedbankAccountNumber(accountNumber);
            return new SwedishIdentifier(completeAccountNumber);
        } else {
            return new SwedishIdentifier(accountNumber);
        }
    }

    /**
     * Danske bank account entities are missing the fifth clearing digit in Swedbank account numbers. Append it to
     * the clearing.
     */
    private String parseCompleteSwedbankAccountNumber(String accountNumber) {
        if (!accountNumber.startsWith("8")) {
            return accountNumber;
        }

        return SwedbankClearingNumberUtils.insertFifthClearingDigit(accountNumber);
    }
}
