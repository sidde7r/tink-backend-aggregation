package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.utils;

import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.formatters.AccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.se.swedbank.SwedbankClearingNumberUtils;

public class IcaBankenAccountIdentifierFormatter implements AccountIdentifierFormatter {

    @Override
    public String apply(AccountIdentifier identifier) {
        if (identifier.is(AccountIdentifierType.SE)) {
            SwedishIdentifier swedishIdentifier = identifier.to(SwedishIdentifier.class);

            switch (swedishIdentifier.getBank()) {
                case SWEDBANK:
                    if (swedishIdentifier.getClearingNumber().startsWith("8")) {
                        return SwedbankClearingNumberUtils
                                .padWithZerosBetweenClearingAndAccountNumber(swedishIdentifier);
                    }
                    break;
                default:
                    break;
            }

            return swedishIdentifier.getIdentifier(new DefaultAccountIdentifierFormatter());
        } else if (identifier.is(AccountIdentifierType.SE_BG)
                || identifier.is(AccountIdentifierType.SE_PG)) {
            return new DisplayAccountIdentifierFormatter().apply(identifier);
        }

        return identifier.getIdentifier(new DefaultAccountIdentifierFormatter());
    }
}
