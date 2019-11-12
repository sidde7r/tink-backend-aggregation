package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.loan;

import java.util.Collection;
import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.loan.rpc.LoansResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

public class SpankkiLoanFetcher implements AccountFetcher<LoanAccount> {
    private final SpankkiApiClient apiClient;

    public SpankkiLoanFetcher(SpankkiApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        final LoansResponse loansResponse = apiClient.fetchLoans();
        if (loansResponse.hasLoans()) {
            loansResponse.logLoans();
            apiClient.fetchLoanDetails().logLoans();
        }
        return Collections.emptyList();
    }
}
