package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.utils;

import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.pair.Pair;

public class AccountTypePair extends Pair<Type, Type> {

    public AccountTypePair(Type type, Type type2) {
        super(type, type2);
    }

    public AccountTypePair(Pair<Type, Type> pair) {
        super(pair.first, pair.second);
    }
}
