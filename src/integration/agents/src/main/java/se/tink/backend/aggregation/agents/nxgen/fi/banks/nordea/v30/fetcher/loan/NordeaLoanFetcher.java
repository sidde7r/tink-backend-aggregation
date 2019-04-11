package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.loan;

import java.util.Collection;
import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.NordeaFiApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.NordeaFiConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.loan.rpc.FetchLoanResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;

public class NordeaLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final AggregationLogger log = new AggregationLogger(NordeaLoanFetcher.class);

    private final NordeaFiApiClient apiClient;

    public NordeaLoanFetcher(NordeaFiApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {

        try {
            HttpResponse response = apiClient.fetchLoans();

            // TODO Loan entity is built from test data found in the Nordea App. Remove when
            // verified.
            log.infoExtraLong(
                    response.getBody(String.class), NordeaFiConstants.LogTags.NORDEA_FI_LOAN);
            return response.getBody(FetchLoanResponse.class).toTinkLoanAccounts();
        } catch (Exception e) {

            return Collections.emptyList();
        }
    }
}
