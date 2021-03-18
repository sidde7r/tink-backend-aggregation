package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment;

import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.pair.Pair;

public class TypePair extends Pair<AccountIdentifierType, AccountIdentifierType> {

    public TypePair(AccountIdentifierType type, AccountIdentifierType type2) {
        super(type, type2);
    }

    public TypePair(Pair<AccountIdentifierType, AccountIdentifierType> pair) {
        super(pair.first, pair.second);
    }
}
