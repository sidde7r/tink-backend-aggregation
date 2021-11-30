package se.tink.agent.sdk.fetching.accounts;

import java.util.List;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

public interface LoanAccountsFetcher {
    List<LoanAccount> fetchAccounts();
}
