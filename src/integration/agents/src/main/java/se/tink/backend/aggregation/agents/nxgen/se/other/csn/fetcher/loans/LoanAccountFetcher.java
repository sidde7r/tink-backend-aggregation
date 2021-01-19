package se.tink.backend.aggregation.agents.nxgen.se.other.csn.fetcher.loans;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.CSNApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.fetcher.loans.rpc.LoanAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.fetcher.loans.rpc.UserInfoResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

@RequiredArgsConstructor
public class LoanAccountFetcher implements AccountFetcher<LoanAccount> {
    private final CSNApiClient apiClient;

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        final LoanAccountsResponse loanAccountsResponse = apiClient.fetchLoanAccounts();
        final UserInfoResponse userInfoResponse = apiClient.fetchUserInfo();
        return loanAccountsResponse.toTinkAccounts(userInfoResponse);
    }
}
