package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.loan;

import java.util.Collection;
import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BBVAApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class BBVALoanFetcher implements AccountFetcher<LoanAccount> {

    private final BBVAApiClient client;

    public BBVALoanFetcher(BBVAApiClient client) {
        this.client = client;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        try {
            return client.fetchLoans().getLoanAccounts();
        } catch (HttpResponseException e) {
            return Collections.EMPTY_LIST;
        }
    }
}
