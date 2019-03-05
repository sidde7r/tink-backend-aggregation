package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.loan;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BBVAApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

public class BBVALoanFetcher implements AccountFetcher<LoanAccount> {

    private final BBVAApiClient client;

    public BBVALoanFetcher(BBVAApiClient client) {
        this.client = client;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        return client.fetchLoans().getLoanAccounts();
    }
}
