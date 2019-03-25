package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment;

import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc.ProductsResponse;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;
import static io.vavr.Predicates.not;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.LogTags.INVESTMENT_INTERNATIONAL_PORTFOLIO;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.LogTags.INVESTMENT_MANAGED_FUNDS;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.LogTags.INVESTMENT_WEALTH_DEPOSITARY;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.LogTags.PRODUCTS_FULL_RESPONSE;

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
        final String holderName = sessionStorage.get(BbvaConstants.StorageKeys.HOLDER_NAME);

        return Try.of(() -> apiClient.fetchProducts())
                .peek(r -> log(r, PRODUCTS_FULL_RESPONSE))
                .peek(r -> log(r.getInternationalFundsPortfolios(), INVESTMENT_INTERNATIONAL_PORTFOLIO))
                .peek(r -> log(r.getManagedFundsPortfolios(), INVESTMENT_MANAGED_FUNDS))
                .peek(r -> log(r.getWealthDepositaryPortfolios(), INVESTMENT_WEALTH_DEPOSITARY))
                .map(ProductsResponse::getStockAccounts)
                .filter(not(List::isEmpty))
                .getOrElse(List.empty())
                .map(stockAccount -> stockAccount.toTinkAccount(apiClient, holderName))
                .toJavaList();
    }

    private void log(Object data, LogTag logTag) {
        Option.of(data)
//                .filter(not(List::isEmpty))
                .toTry()
                .onSuccess(d -> LOGGER.infoExtraLong(SerializationUtils.serializeToString(d), logTag))
                .onFailure(e -> LOGGER.warn(logTag.toString() + " - Failed to log investment data, " + e.getMessage()));
    }
}
