package se.tink.libraries.account.identifiers.formatters;

import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.GiroIdentifier;

public class DefaultAccountIdentifierFormatter implements AccountIdentifierFormatter {
    @Override
    public String apply(AccountIdentifier identifier) {
        if (identifier.is(AccountIdentifier.Type.SE_BG)
                || identifier.is(AccountIdentifier.Type.SE_PG)) {
            return identifier.to(GiroIdentifier.class).getGiroNumber();
        }

        return identifier.getIdentifier();
    }
}
