package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.entities.MarketsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.entities.StocksEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc.MarketsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc.SavingsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc.ServicingFundsAccountDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc.ServicingFundsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
public class SabadellInvestmentFetcher implements AccountFetcher<InvestmentAccount> {

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

        try {
            List<InvestmentAccount> allInvestmentAccounts = new ArrayList<>();
            List<InvestmentAccount> stockInvestmentAccounts =
                    apiClient.fetchProducts().getInvestmentProduct().getSecurities().getAccounts()
                            .stream()
                            .map(aggregateStockInvestmentAccount())
                            .collect(Collectors.toList());

            allInvestmentAccounts.addAll(stockInvestmentAccounts);

            return allInvestmentAccounts;
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();
            ErrorResponse errorResponse = response.getBody(ErrorResponse.class);
            String errorCode = errorResponse.getCode();

            if (Strings.isNullOrEmpty(errorCode)
                    && HttpStatus.SC_INTERNAL_SERVER_ERROR == response.getStatus()) {
                return Collections.emptyList();
            }
            log.warn(
                    "{}: Investment fetching failed with error code: {}, error message: {}",
                    SabadellConstants.Tags.INVESTMENTS_ERROR,
                    errorResponse.getErrorCode(),
                    errorResponse.getErrorMessage());
            throw e;
        }
    }

    private Function<AccountEntity, InvestmentAccount> aggregateStockInvestmentAccount() {
        return accountEntity -> {
            List<Instrument> instruments = fetchMarkets(accountEntity);
            List<Portfolio> portfolios = accountEntity.toTinkPortfolios(instruments);
            return accountEntity.toTinkInvestmentAccount(portfolios);
        };
    }

    private List<Instrument> fetchMarkets(AccountEntity accountEntity) {
        try {
            return apiClient.fetchMarkets(new MarketsRequest(accountEntity)).getMarkets().stream()
                    .flatMap(getInstruments(accountEntity))
                    .collect(Collectors.toList());
        } catch (HttpResponseException e) {
            ErrorResponse response = e.getResponse().getBody(ErrorResponse.class);
            String errorCode = response.getErrorCode();
            if (ErrorCodes.NO_TRANSACTIONS.equalsIgnoreCase(errorCode)) {
                return Collections.emptyList();
            }
            log.warn(
                    "Investment fetching failed with error code: {}, error message: {}",
                    response.getErrorCode(),
                    response.getErrorMessage());
        }
        return Collections.emptyList();
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
            // will be logged to s3
            apiClient.fetchDeposits();
        } catch (Exception e) {
            log.warn(
                    String.format(
                            "%s could not fetch deposits", SabadellConstants.Tags.DEPOSITS_ERROR),
                    e);
        }
    }

    private void logServicingFunds() {
        // will be logged to s3
        try {
            ServicingFundsResponse servicingFundsResponse = apiClient.fetchServicingFunds();

            if (!servicingFundsResponse.getAccountList().getAccounts().isEmpty()
                    || servicingFundsResponse.getFundList() != null) {

                servicingFundsResponse
                        .getAccountList()
                        .getAccounts()
                        .forEach(
                                account ->
                                        apiClient.fetchServicingFundsAccountDetails(
                                                ServicingFundsAccountDetailsRequest
                                                        .createRequestFromAccount(account)));
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
            // will be logged to s3
            apiClient.fetchPensionPlans();
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
            // will be logged to s3
            SavingsResponse savingsResponse = apiClient.fetchSavings();

            if (!savingsResponse.getSavingPlans().isEmpty()) {
                savingsResponse
                        .getSavingPlans()
                        .forEach(
                                savingsPlan ->
                                        apiClient.fetchSavingsPlanDetails(
                                                savingsPlan.getQueryParamsForDetailsRequest()));
            }
        } catch (Exception e) {
            log.warn(
                    String.format(
                            "%s could not fetch savings", SabadellConstants.Tags.SAVINGS_ERROR),
                    e);
        }
    }
}
