package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.loan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.SpankkiApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.loan.entities.LoanOverviewEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;

public class SpankkiLoanFetcher implements AccountFetcher<LoanAccount> {

    private final SpankkiApiClient apiClient;

    public SpankkiLoanFetcher(SpankkiApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        List<LoanAccount> loanAccounts = new ArrayList<>();

        List<LoanOverviewEntity> loanOverviews = this.apiClient.fetchLoanOverview().getLoanOverviews();

        if (loanOverviews != null) {

            for (LoanOverviewEntity loanOverview : loanOverviews) {
                LoanDetailsResponse loandetailsResponse = this.apiClient
                        .fetchLoanDetails(loanOverview.getLoanNumber(), loanOverview.getLoanType());

                loanAccounts.add(loandetailsResponse.toTinkLoanAccount());
            }
        }

        return loanAccounts;
    }
}
