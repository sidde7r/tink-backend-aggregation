package se.tink.backend.aggregation.agents.consent.generators.uk.starling;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.consent.Scope;

@RequiredArgsConstructor
public enum StarlingScope implements Scope {
    ACCOUNT_READ("account:read"),
    BALANCE_READ("balance:read"),
    TRANSACTION_READ("transaction:read"),
    ACCOUNT_HOLDER_TYPE_READ("account-holder-type:read"),
    ACCOUNT_HOLDER_NAME_READ("account-holder-name:read"),
    ACCOUNT_IDENTIFIER_READ("account-identifier:read");

    private final String value;

    @Override
    public String toString() {
        return value;
    }
}
