package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment;

import java.util.Collection;
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
        ProductsResponse products = apiClient.fetchProducts();

        return products.getStockAccounts()
                .map(this::toInvestmentAccount)
                .appendAll(products.getPensionPlans().map(this::toInvestmentAccount))
                .asJava();
    }

    private InvestmentAccount toInvestmentAccount(StockAccountEntity stock) {
        String holderName = sessionStorage.get(BbvaConstants.StorageKeys.HOLDER_NAME);
        return stock.toTinkAccount(apiClient, holderName);
    }

    private InvestmentAccount toInvestmentAccount(PensionPlansEntity pension) {
        String holderName = sessionStorage.get(BbvaConstants.StorageKeys.HOLDER_NAME);
        return pension.toTinkAccount(holderName);
    }
}
