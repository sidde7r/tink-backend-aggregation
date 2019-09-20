package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities.PensionPlansEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities.StockAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc.ProductsResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BbvaInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private static final AggregationLogger LOGGER =
            new AggregationLogger(BbvaInvestmentFetcher.class);
    private final SessionStorage sessionStorage;
    private BbvaApiClient apiClient;

    public BbvaInvestmentFetcher(BbvaApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        List<InvestmentAccount> investmentAccounts = new ArrayList<>();
        final String holderName = sessionStorage.get(BbvaConstants.StorageKeys.HOLDER_NAME);

        ProductsResponse products = apiClient.fetchProducts();

        if (products.getStockAccounts() != null) {
            investmentAccounts.addAll(
                    getInvestmentAccountsFromStocks(
                            products.getStockAccounts().asJava(), holderName));
        }

        if (products.getPensionPlans() != null) {
            investmentAccounts.addAll(
                    getInvestmentAccountsFromPensions(
                            products.getPensionPlans().asJava(), holderName));
        }

        return investmentAccounts;
    }

    private List<InvestmentAccount> getInvestmentAccountsFromStocks(
            List<StockAccountEntity> stocks, String holderName) {
        return stocks.stream()
                .map(stockAccount -> stockAccount.toTinkAccount(apiClient, holderName))
                .collect(Collectors.toList());
    }

    private List<InvestmentAccount> getInvestmentAccountsFromPensions(
            List<PensionPlansEntity> pensions, String holderName) {
        return pensions.stream()
                .map(pensionPlan -> pensionPlan.toTinkAccount(holderName))
                .collect(Collectors.toList());
    }
}
