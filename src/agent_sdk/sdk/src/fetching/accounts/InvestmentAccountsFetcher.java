package se.tink.agent.sdk.fetching.accounts;

import java.util.List;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

public interface InvestmentAccountsFetcher {
    List<InvestmentAccount> fetchAccounts();
}
