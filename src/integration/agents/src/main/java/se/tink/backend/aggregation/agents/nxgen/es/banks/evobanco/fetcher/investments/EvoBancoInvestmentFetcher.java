package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.investments;

import java.util.Collection;
import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

public class EvoBancoInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private final AggregationLogger logger = new AggregationLogger(EvoBancoInvestmentFetcher.class);
    private final EvoBancoApiClient apiClient;

    public EvoBancoInvestmentFetcher(EvoBancoApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        logInvestments();

        return Collections.emptyList();
    }

    private void logInvestments() {
        try {
            apiClient.fetchInvestments();
        } catch (Exception e) {
            logger.warn(
                    String.format(
                            "%s could not fetch investments",
                            EvoBancoConstants.Tags.INVESTMENTS_ERROR),
                    e);
        }
    }
}
