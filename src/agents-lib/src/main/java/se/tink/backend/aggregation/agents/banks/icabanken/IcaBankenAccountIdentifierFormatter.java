package se.tink.backend.aggregation.agents.banks.icabanken;

import se.tink.libraries.account.identifiers.se.swedbank.SwedbankClearingNumberUtils;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.formatters.AccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;

public class IcaBankenAccountIdentifierFormatter implements AccountIdentifierFormatter {

    @Override
    public String apply(AccountIdentifier identifier) {
        if (identifier.is(Type.SE)) {
            SwedishIdentifier swedishIdentifier = identifier.to(SwedishIdentifier.class);

            switch (swedishIdentifier.getBank()) {
            case SWEDBANK:
                if (swedishIdentifier.getClearingNumber().startsWith("8")) {
                    return SwedbankClearingNumberUtils.padWithZerosBetweenClearingAndAccountNumber(swedishIdentifier);
                }
                break;
            default:
                break;
            }

            return swedishIdentifier.getIdentifier(new DefaultAccountIdentifierFormatter());
        } else if (identifier.is(Type.SE_BG) || identifier.is(Type.SE_PG)) {
            return new DisplayAccountIdentifierFormatter().apply(identifier);
        }

        return identifier.getIdentifier(new DefaultAccountIdentifierFormatter());
    }
}
