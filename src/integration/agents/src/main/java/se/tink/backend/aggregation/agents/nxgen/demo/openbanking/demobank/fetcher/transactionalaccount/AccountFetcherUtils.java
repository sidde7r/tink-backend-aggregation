package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount;

import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.nxgen.core.account.AccountHolderType;

public class AccountFetcherUtils {
    public static AccountHolderType getHolderTypeSupportedByProvider(Provider provider) {
        switch (provider.getAuthenticationUserType()) {
            case BUSINESS:
                return AccountHolderType.BUSINESS;
            case PERSONAL:
                return AccountHolderType.PERSONAL;
            case CORPORATE:
                return AccountHolderType.CORPORATE;
            default:
                return AccountHolderType.PERSONAL;
        }
    }
}
