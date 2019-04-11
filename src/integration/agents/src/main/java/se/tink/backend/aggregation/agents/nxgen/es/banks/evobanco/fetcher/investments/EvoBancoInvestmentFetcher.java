package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.investments;

import java.util.Collection;
import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.investments.rpc.InvestmentsResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class EvoBancoInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private final AggregationLogger log = new AggregationLogger(EvoBancoInvestmentFetcher.class);
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
            InvestmentsResponse investmentsResponse = apiClient.fetchInvestments();

            if (!investmentsResponse.getInvestments().isEmpty()) {
                log.infoExtraLong(
                        SerializationUtils.serializeToString(investmentsResponse),
                        EvoBancoConstants.Tags.INVESTMENTS);
            }
        } catch (Exception e) {
            log.warn(
                    String.format(
                            "%s could not fetch investments",
                            EvoBancoConstants.Tags.INVESTMENTS_ERROR),
                    e);
        }
    }
}
