package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.investment;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities.FinancialMarketAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities.PositionEntity;
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
        return apiClient
                .getPositions()
                .map(PositionEntity::getFinancialMarketAccounts)
                .map(mapToTinkInvestment())
                .get();
    }

    private Function<List<FinancialMarketAccountEntity>, List<InvestmentAccount>>
            mapToTinkInvestment() {
        return financialMarketAccounts ->
                financialMarketAccounts.stream()
                        .map(
                                financialMarketAccountEntity ->
                                        financialMarketAccountEntity.toTinkInvestmentAccount(
                                                fetchInvestmentAccountDetails(
                                                        financialMarketAccountEntity
                                                                .getAccountId())))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
    }

    private InvestmentAccountResponse fetchInvestmentAccountDetails(String accountId) {
        return apiClient.fetchInvestmentAccountDetails(accountId);
    }
}
