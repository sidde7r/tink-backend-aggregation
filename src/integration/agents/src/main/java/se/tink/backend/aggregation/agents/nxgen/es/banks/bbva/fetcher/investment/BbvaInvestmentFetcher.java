package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment;

import io.vavr.collection.List;
import io.vavr.control.Option;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.PostParameter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.ContractEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.PensionPlanEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.PositionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.SecuritiesPortfolioEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc.FinancialInvestmentRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc.HistoricalDateRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc.HistoricalDateResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

public class BbvaInvestmentFetcher implements AccountFetcher<InvestmentAccount> {

    private BbvaApiClient apiClient;

    public BbvaInvestmentFetcher(BbvaApiClient apiClient) {
        this.apiClient = apiClient;
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
                .map(this::mapSecurityTotInvestmentAccount);
    }

    private List<InvestmentAccount> getPensionPlans(List<PositionEntity> positions) {
        return positions
                .map(PositionEntity::getContract)
                .map(ContractEntity::getPensionPlan)
                .filter(Option::isDefined)
                .map(Option::get)
                .map(this::mapPensionTotInvestmentAccount);
    }

    private InvestmentAccount mapSecurityTotInvestmentAccount(SecuritiesPortfolioEntity portfolio) {
        Double totalProfit = getPortfolioTotalProfit(portfolio.getId(), portfolio.getBalance());
        return portfolio.toInvestmentAccount(totalProfit);
    }

    private InvestmentAccount mapPensionTotInvestmentAccount(PensionPlanEntity portfolio) {
        Double totalProfit = getPortfolioTotalProfit(portfolio.getId(), portfolio.getBalance());
        return portfolio.toInvestmentAccount(totalProfit);
    }

    private Double getPortfolioTotalProfit(String portfolio, AmountEntity balance) {
        HistoricalDateRequest historicalDateRequest = new HistoricalDateRequest(portfolio);
        HistoricalDateResponse historicalDateResponse =
                apiClient.fetchInvestmentHistoricalDate(historicalDateRequest);
        FinancialInvestmentRequest financialInvestmentRequest =
                new FinancialInvestmentRequest(
                        portfolio,
                        Optional.ofNullable(historicalDateResponse.getMaxHistoricalDate())
                                .orElseGet(this::getFallbackStartDate),
                        balance.toTinkAmount().getCurrencyCode(),
                        balance.getAmount());
        return apiClient.fetchFinancialInvestment(financialInvestmentRequest).getTotalProfit();
    }

    private Date getFallbackStartDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, PostParameter.START_DATE_YEAR_AGO);
        return calendar.getTime();
    }
}
