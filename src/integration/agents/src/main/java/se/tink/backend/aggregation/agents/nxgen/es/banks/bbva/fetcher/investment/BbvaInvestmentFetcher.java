package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment;

import io.vavr.collection.List;
import io.vavr.control.Option;
import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.ContractEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.PensionPlanEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.PositionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.SecuritiesPortfolioEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc.FinancialInvestmentRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc.HistoricalDateRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc.HistoricalDateResponse;
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
        List<PositionEntity> positions = apiClient.fetchFinancialDashboard().getPositions();
        return getSecurities(positions).appendAll(getPensionPlans(positions)).asJava();
    }

    private List<InvestmentAccount> getSecurities(List<PositionEntity> positions) {
        return positions
                .map(PositionEntity::getContract)
                .map(ContractEntity::getSecuritiesPortfolio)
                .filter(Option::isDefined)
                .map(Option::get)
                .map(this::getInvestmentAccountFromSecurity);
    }

    private InvestmentAccount getInvestmentAccountFromSecurity(
            SecuritiesPortfolioEntity portfolio) {
        Double totalProfit = getSecurituTotalProfit(portfolio);
        return portfolio.toInvestmentAccount(totalProfit);
    }

    private Double getSecurituTotalProfit(SecuritiesPortfolioEntity portfolio) {
        String id = portfolio.getId();
        HistoricalDateRequest historicalDateRequest = new HistoricalDateRequest(id);
        HistoricalDateResponse historicalDateResponse =
                apiClient.fetchInvestmentHistoricalDate(historicalDateRequest);
        FinancialInvestmentRequest financialInvestmentRequest =
                new FinancialInvestmentRequest(
                        id,
                        historicalDateResponse.getMaxHistoricalDate(),
                        portfolio.getCurrency().getId(),
                        portfolio.getBalance().getAmount());
        return apiClient.fetchFinancialInvestment(financialInvestmentRequest).getTotalProfit();
    }

    private List<InvestmentAccount> getPensionPlans(List<PositionEntity> positions) {
        return positions
                .map(PositionEntity::getContract)
                .map(ContractEntity::getPensionPlan)
                .filter(Option::isDefined)
                .map(Option::get)
                .map(PensionPlanEntity::toInvestmentAccount);
    }
}
