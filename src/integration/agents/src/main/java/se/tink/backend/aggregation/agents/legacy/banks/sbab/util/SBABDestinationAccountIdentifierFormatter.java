package se.tink.backend.aggregation.agents.banks.sbab.util;

import com.google.common.base.Strings;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.formatters.AccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;

public class SBABDestinationAccountIdentifierFormatter implements AccountIdentifierFormatter {
    private static final DefaultAccountIdentifierFormatter DEFAULT_FORMATTER = new DefaultAccountIdentifierFormatter();

    @Override
    public String apply(AccountIdentifier identifier) {
        if (identifier.is(AccountIdentifier.Type.SE)) {
            return toSBABDestination(identifier.to(SwedishIdentifier.class));
        }
        return identifier.getIdentifier(DEFAULT_FORMATTER);
    }

    public String toSBABDestination(SwedishIdentifier swedishIdentifier) {
        String accountNumber = swedishIdentifier.getAccountNumber();
        String clearingNumber = swedishIdentifier.getClearingNumber();

        switch (swedishIdentifier.getBank()) {
        case HANDELSBANKEN:
            if (accountNumber.length() < 13) {
                accountNumber = Strings.padStart(accountNumber, 9, '0');
            }
            break;
        case DANSKE_BANK_SVERIGE:
        case SPARBANKEN_SYD:
        case SPARBANKEN_ORESUND:
        case PLUSGIROT_BANK:
            if (accountNumber.length() < 14) {
                accountNumber = Strings.padStart(accountNumber, 10, '0');
            }
            break;
        case SWEDBANK:
            if (clearingNumber.length() == 5 && accountNumber.length() < 15) {
                accountNumber = Strings.padStart(accountNumber, 10, '0');
            }
            break;
        default:
            return swedishIdentifier.getIdentifier();
        }

        return clearingNumber + accountNumber;
    }
}
