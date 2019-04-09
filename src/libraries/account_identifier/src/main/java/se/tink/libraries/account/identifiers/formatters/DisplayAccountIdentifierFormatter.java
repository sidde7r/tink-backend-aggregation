package se.tink.libraries.account.identifiers.formatters;

import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

public class DisplayAccountIdentifierFormatter implements AccountIdentifierFormatter {
    @Override
    public String apply(AccountIdentifier identifier) {
        if (!identifier.isValid()) {
            throw new IllegalArgumentException(
                    "Developer should check validity before using a formatter");
        }

        switch (identifier.getType()) {
            case SE:
                return formatSwedishAcount((SwedishIdentifier) identifier);
            case SE_BG:
                return formatBankGiro((BankGiroIdentifier) identifier);
            case SE_PG:
                return formatPlusGiro((PlusGiroIdentifier) identifier);
            case IBAN:
                return formatIban((IbanIdentifier) identifier);
            case TINK:
                return null;
            default:
                return identifier.getIdentifier(new DefaultAccountIdentifierFormatter());
        }
    }

    private String formatIban(IbanIdentifier identifier) {
        return identifier
                .getIban()
                .replaceAll("(.{4})(?!$)", "$1 "); // Format in groups of four from the beginning
    }

    private String formatSwedishAcount(SwedishIdentifier identifier) {
        String clearingNumber = identifier.getClearingNumber();
        String accountNumber = identifier.getAccountNumber();

        if (clearingNumber.length() == 5) {
            return String.format(
                    "%s-%s,%s",
                    clearingNumber.substring(0, 4), clearingNumber.substring(4, 5), accountNumber);
        }
        return String.format("%s-%s", clearingNumber, accountNumber);
    }

    private String formatPlusGiro(PlusGiroIdentifier identifier) {
        String accountNumber = identifier.getGiroNumber();
        return String.format(
                "%s-%s",
                accountNumber.substring(0, accountNumber.length() - 1),
                accountNumber.substring(accountNumber.length() - 1));
    }

    private String formatBankGiro(BankGiroIdentifier identifier) {
        String accountNumber = identifier.getGiroNumber();
        return String.format(
                "%s-%s",
                accountNumber.substring(0, accountNumber.length() - 4),
                accountNumber.substring(accountNumber.length() - 4));
    }
}
