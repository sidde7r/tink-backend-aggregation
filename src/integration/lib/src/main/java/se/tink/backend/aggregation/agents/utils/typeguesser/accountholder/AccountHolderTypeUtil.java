package se.tink.backend.aggregation.agents.utils.typeguesser.accountholder;

import se.tink.backend.agents.rpc.AccountHolderType;
import se.tink.backend.agents.rpc.Provider;

public class AccountHolderTypeUtil {

    public static AccountHolderType inferHolderType(Provider provider) {
        boolean supportsPersonal = provider.hasSupportForPersonalSegment();
        boolean supportsBusiness = provider.hasSupportForBusinessSegment();
        if (supportsPersonal && supportsBusiness) {
            return AccountHolderType.UNKNOWN;
        }
        if (supportsPersonal) {
            return AccountHolderType.PERSONAL;
        }
        if (supportsBusiness) {
            return AccountHolderType.BUSINESS;
        }
        return AccountHolderType.UNKNOWN;
    }
}
