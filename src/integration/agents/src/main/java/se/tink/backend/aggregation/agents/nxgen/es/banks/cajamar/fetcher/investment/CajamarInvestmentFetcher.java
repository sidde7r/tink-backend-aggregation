package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.investment;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.investment.rpc.InvestmentAccountResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

public class CajamarInvestmentFetcher implements AccountFetcher<InvestmentAccount> {

    private final CajamarApiClient apiClient;

    public CajamarInvestmentFetcher(CajamarApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        return apiClient.fetchPositions().getFinancialMarketAccounts().stream()
                .map(
                        financialMarketAccountEntity ->
                                financialMarketAccountEntity.toTinkInvestmentAccount(
                                        fetchInvestmentAccountDetails(
                                                financialMarketAccountEntity.getAccountId())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private InvestmentAccountResponse fetchInvestmentAccountDetails(String accountId) {
        return apiClient.fetchInvestmentAccountDetails(accountId);
    }
}
