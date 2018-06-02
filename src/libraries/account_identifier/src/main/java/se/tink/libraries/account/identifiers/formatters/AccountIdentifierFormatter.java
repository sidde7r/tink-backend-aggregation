package se.tink.libraries.account.identifiers.formatters;

import se.tink.libraries.account.AccountIdentifier;

public interface AccountIdentifierFormatter {

    String apply(AccountIdentifier identifier);
}
