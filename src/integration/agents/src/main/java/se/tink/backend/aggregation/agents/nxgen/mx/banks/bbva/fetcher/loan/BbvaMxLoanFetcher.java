package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.loan;

import java.util.Collection;
import java.util.Collections;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BbvaMxApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class BbvaMxLoanFetcher implements AccountFetcher<LoanAccount> {

    private final BbvaMxApiClient client;

    public BbvaMxLoanFetcher(BbvaMxApiClient client) {
        this.client = client;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        try {
            return client.fetchLoans().getLoanAccounts();
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_NO_CONTENT) {
                return Collections.emptyList();
            }
            throw e;
        }
    }
}
