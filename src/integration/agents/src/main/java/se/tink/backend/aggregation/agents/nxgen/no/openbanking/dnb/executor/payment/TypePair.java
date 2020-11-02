package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment;

import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.pair.Pair;

public class TypePair extends Pair<Type, Type> {

    public TypePair(Type type, Type type2) {
        super(type, type2);
    }

    public TypePair(Pair<Type, Type> pair) {
        super(pair.first, pair.second);
    }
}
