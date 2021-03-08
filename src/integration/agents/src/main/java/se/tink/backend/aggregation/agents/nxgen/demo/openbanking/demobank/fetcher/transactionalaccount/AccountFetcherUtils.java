package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount;

import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.utils.typeguesser.accountholder.AccountHolderTypeUtil;
import se.tink.backend.aggregation.nxgen.core.account.AccountHolderType;

public class AccountFetcherUtils {
    public static AccountHolderType inferHolderTypeFromProvider(Provider provider) {
        String accountHolderTypeAsString = AccountHolderTypeUtil.inferHolderType(provider).name();
        return AccountHolderType.valueOf(accountHolderTypeAsString);
    }
}
