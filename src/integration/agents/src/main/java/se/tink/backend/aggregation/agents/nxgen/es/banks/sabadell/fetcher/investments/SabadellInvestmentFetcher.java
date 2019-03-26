package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments;

import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.entities.MarketsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.entities.StocksEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc.DepositsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc.MarketsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc.PensionPlansResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc.SavingsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc.ServicingFundsAccountDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc.ServicingFundsResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SabadellInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private final AggregationLogger log = new AggregationLogger(SabadellInvestmentFetcher.class);
    private final SabadellApiClient apiClient;

    public SabadellInvestmentFetcher(SabadellApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        logDeposits();
        logPensionPlans();
        logSavings();
        logServicingFunds();

        List<InvestmentAccount> allInvestmentAccounts = new ArrayList<>();

        List<InvestmentAccount> stockInvestmentAccounts =
                apiClient.fetchProducts().getInvestmentProduct().getSecurities().getAccounts()
                        .stream()
                        .map(aggregateStockInvestmentAccount())
                        .collect(Collectors.toList());

        allInvestmentAccounts.addAll(stockInvestmentAccounts);
        return allInvestmentAccounts;
    }

    private Function<AccountEntity, InvestmentAccount> aggregateStockInvestmentAccount() {
        return accountEntity -> {
            List<Instrument> instruments =
                    apiClient.fetchMarkets(new MarketsRequest(accountEntity)).getMarkets().stream()
                            .flatMap(getInstruments(accountEntity))
                            .collect(Collectors.toList());

            List<Portfolio> portfolios = accountEntity.toTinkPortfolios(instruments);

            return accountEntity.toTinkInvestmentAccount(portfolios);
        };
    }

    private Function<MarketsEntity, Stream<? extends Instrument>> getInstruments(
            AccountEntity accountEntity) {

        return marketsEntity ->
                apiClient
                        .fetchStocks(
                                marketsEntity.getName().toLowerCase(),
                                accountEntity.getMappedAttributes())
                        .getStocks().stream()
                        .map(StocksEntity::toTinkInstrument);
    }

    private void logDeposits() {
        try {
            DepositsResponse depositsResponse = apiClient.fetchDeposits();

            if (!depositsResponse.getAccountsPositions().isEmpty()) {
                log.infoExtraLong(
                        SerializationUtils.serializeToString(depositsResponse),
                        SabadellConstants.Tags.DEPOSITS);
            }
        } catch (Exception e) {
            log.warn(
                    String.format(
                            "%s could not fetch deposits", SabadellConstants.Tags.DEPOSITS_ERROR),
                    e);
        }
    }

    private void logServicingFunds() {
        try {
            ServicingFundsResponse servicingFundsResponse = apiClient.fetchServicingFunds();

            if (!servicingFundsResponse.getAccountList().getAccounts().isEmpty()
                    || servicingFundsResponse.getFundList() != null) {
                log.infoExtraLong(
                        SerializationUtils.serializeToString(servicingFundsResponse),
                        SabadellConstants.Tags.SERVICING_FUNDS);

                servicingFundsResponse
                        .getAccountList()
                        .getAccounts()
                        .forEach(
                                account -> {
                                    String detailsResponse =
                                            apiClient.fetchServicingFundsAccountDetails(
                                                    ServicingFundsAccountDetailsRequest
                                                            .createRequestFromAccount(account));

                                    log.infoExtraLong(
                                            detailsResponse,
                                            SabadellConstants.Tags.SERVICING_FUNDS_ACCOUNT_DETAILS);
                                });
            }
        } catch (Exception e) {
            log.warn(
                    String.format(
                            "%s could not fetch servicing funds",
                            SabadellConstants.Tags.SERVICING_FUNDS_ERROR),
                    e);
        }
    }

    private void logPensionPlans() {
        try {
            PensionPlansResponse pensionPlansResponse = apiClient.fetchPensionPlans();

            if (!pensionPlansResponse.getAccounts().isEmpty()) {
                log.infoExtraLong(
                        SerializationUtils.serializeToString(pensionPlansResponse),
                        SabadellConstants.Tags.PENSION_PLANS);
            }
        } catch (Exception e) {
            log.warn(
                    String.format(
                            "%s could not fetch pension plans",
                            SabadellConstants.Tags.PENSION_PLANS_ERROR),
                    e);
        }
    }

    private void logSavings() {
        try {
            SavingsResponse savingsResponse = apiClient.fetchSavings();

            if (!savingsResponse.getSavingPlans().isEmpty()) {
                log.infoExtraLong(
                        SerializationUtils.serializeToString(savingsResponse),
                        SabadellConstants.Tags.SAVINGS);

                savingsResponse
                        .getSavingPlans()
                        .forEach(
                                savingsPlan -> {
                                    String detailsResponse =
                                            apiClient.fetchSavingsPlanDetails(savingsPlan.getQueryParamsForDetailsRequest());

                                    log.infoExtraLong(
                                            detailsResponse,
                                            SabadellConstants.Tags.SAVINGS_PLAN_DETAILS);
                                });
            }
        } catch (Exception e) {
            log.warn(
                    String.format(
                            "%s could not fetch savings", SabadellConstants.Tags.SAVINGS_ERROR),
                    e);
        }
    }
}
