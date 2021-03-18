package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.util;

import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.pair.Pair;

public class AccountTypePair extends Pair<AccountIdentifierType, AccountIdentifierType> {

    public AccountTypePair(AccountIdentifierType type, AccountIdentifierType type2) {
        super(type, type2);
    }

    public AccountTypePair(Pair<AccountIdentifierType, AccountIdentifierType> pair) {
        super(pair.first, pair.second);
    }

    public AccountIdentifierType getCreditorAccountType() {
        return super.second;
    }

    public AccountIdentifierType getDebtorAccountType() {
        return super.first;
    }
}
