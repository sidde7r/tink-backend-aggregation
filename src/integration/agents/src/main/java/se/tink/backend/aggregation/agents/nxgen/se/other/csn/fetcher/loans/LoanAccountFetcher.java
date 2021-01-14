package se.tink.backend.aggregation.agents.nxgen.se.other.csn.fetcher.loans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
        List<LoanAccount> loanAccounts = new ArrayList<>();
        final LoanAccountsResponse loanAccountsResponse = apiClient.fetchLoanAccounts();
        final UserInfoResponse userInfoResponse = apiClient.fetchUserInfo();
        final LoanAccount loanAccount = loanAccountsResponse.toTinkLoanAccount(userInfoResponse);
        loanAccounts.add(loanAccount);
        return loanAccounts;
    }
}
