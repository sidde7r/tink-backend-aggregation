package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class DetailedPortfolioEntity extends AbstractInvestmentAccountEntity {
    @JsonIgnore
    private static final Logger log = LoggerFactory.getLogger(DetailedPortfolioEntity.class);

    @JsonIgnore private static final String API_CLIENT_ERROR_MESSAGE = "No API client provided.";

    private AmountEntity acquisitionValue;
    private ChartDataEntity chartData;
    private TotalEquitiesEntity totalEquities;
    private List<PlacementEntity> placements;
    private List<SettlementEntity> settlements;
    private List<OperationEntity> operations;
    private String fundAccountNumber;
    private String fundAccountName;
    private String encompassedHoldings;

    public AmountEntity getAcquisitionValue() {
        return acquisitionValue;
    }

    public ChartDataEntity getChartData() {
        return chartData;
    }

    public TotalEquitiesEntity getTotalEquities() {
        return totalEquities;
    }

    public List<PlacementEntity> getPlacements() {
        return placements;
    }

    public List<SettlementEntity> getSettlements() {
        return settlements;
    }

    public List<OperationEntity> getOperations() {
        return operations;
    }

    public String getFundAccountNumber() {
        return fundAccountNumber;
    }

    public String getFundAccountName() {
        return fundAccountName;
    }

    public String getEncompassedHoldings() {
        return encompassedHoldings;
    }

    public Optional<InvestmentAccount> toTinkFundInvestmentAccount(
            SwedbankSEApiClient apiClient, String defaultCurrency) {
        validateInvestmentAccountArguments(apiClient, defaultCurrency);

        List<Instrument> instruments = toTinkFundInstruments(apiClient);
        Optional<Portfolio> portfolio = toTinkPortfolio(instruments, defaultCurrency);

        if (!portfolio.isPresent()) {
            return Optional.empty();
        }

        return createTinkInvestmentAccount(
                this.fullyFormattedNumber,
                portfolio.map(Collections::singletonList).orElseGet(Collections::emptyList),
                defaultCurrency,
                this.marketValue,
                this.name);
    }

    public Optional<InvestmentAccount> toTinkInvestmentAccount(
            SwedbankSEApiClient apiClient, String defaultCurrency) {
        validateInvestmentAccountArguments(apiClient, defaultCurrency);

        List<Instrument> instruments = toTinkInstruments(apiClient);
        Optional<Portfolio> portfolio = toTinkPortfolio(instruments, defaultCurrency);
        if (!portfolio.isPresent()) {
            return Optional.empty();
        }

        return createTinkInvestmentAccount(
                this.fullyFormattedNumber,
                portfolio.map(Collections::singletonList).orElseGet(Collections::emptyList),
                defaultCurrency,
                this.marketValue,
                this.name);
    }

    private static Optional<InvestmentAccount> createTinkInvestmentAccount(
            String accountNumber,
            List<Portfolio> portfolios,
            String defaultCurrency,
            AmountEntity marketValue,
            String name) {
        if (accountNumber == null || marketValue == null || marketValue.getAmount() == null) {
            return Optional.empty();
        }

        // sum cash value from portfolios and add to account
        double cashBalance = portfolios.stream().mapToDouble(Portfolio::getCashValue).sum();

        return Optional.of(
                InvestmentAccount.builder(accountNumber)
                        .setCashBalance(ExactCurrencyAmount.of(cashBalance, defaultCurrency))
                        .setAccountNumber(accountNumber)
                        .setName(name)
                        .setPortfolios(portfolios)
                        .build());
    }

    private static void validateInvestmentAccountArguments(
            SwedbankSEApiClient apiClient, String defaultCurrency) {
        Preconditions.checkNotNull(apiClient, API_CLIENT_ERROR_MESSAGE);
        Preconditions.checkNotNull(defaultCurrency, "No default currency provided.");
    }

    private Optional<Portfolio> toTinkPortfolio(
            List<Instrument> instruments, String defaultCurrency) {
        Preconditions.checkNotNull(
                instruments, "You need to at least provide a empty list of instruments.");
        Portfolio portfolio = new Portfolio();

        portfolio.setType(getTinkPortfolioType());
        portfolio.setTotalValue(
                Optional.ofNullable(this.marketValue)
                        .map(amountEntity -> amountEntity.toTinkAmount(defaultCurrency))
                        .map(ExactCurrencyAmount::getDoubleValue)
                        .orElse(null));
        portfolio.setRawType(this.type != null ? this.type : "");
        portfolio.setTotalProfit(
                Optional.ofNullable(this.performance)
                        .map(performanceEntity -> performanceEntity.getTinkAmount(defaultCurrency))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(ExactCurrencyAmount::getDoubleValue)
                        .orElse(null));

        portfolio.setCashValue(
                Optional.ofNullable(this.settlements).orElseGet(Collections::emptyList).stream()
                        .map(SettlementEntity::getBuyingPower)
                        .map(AmountEntity::toTinkAmount)
                        .mapToDouble(ExactCurrencyAmount::getDoubleValue)
                        .sum());

        portfolio.setInstruments(instruments);
        portfolio.setUniqueIdentifier(this.accountNumber);

        return Optional.of(portfolio);
    }

    private List<Instrument> toTinkFundInstruments(SwedbankSEApiClient apiClient) {
        Preconditions.checkNotNull(apiClient, API_CLIENT_ERROR_MESSAGE);

        return Optional.ofNullable(placements).orElseGet(Collections::emptyList).stream()
                .map(placementEntity -> placementEntity.toTinkFundInstruments(apiClient))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private List<Instrument> toTinkInstruments(SwedbankSEApiClient apiClient) {
        Preconditions.checkNotNull(apiClient, API_CLIENT_ERROR_MESSAGE);

        return Optional.ofNullable(placements).orElseGet(Collections::emptyList).stream()
                .map(placementEntity -> placementEntity.toTinkInstruments(apiClient))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private Portfolio.Type getTinkPortfolioType() {
        if (this.type == null) {
            return Portfolio.Type.OTHER;
        }

        switch (SwedbankBaseConstants.InvestmentAccountType.fromAccountType(type)) {
            case ISK:
                return Portfolio.Type.ISK;
            case FUNDACCOUNT:
            case EQUITY_TRADER:
                return Portfolio.Type.DEPOT;
            case ENDOWMENT:
                return Portfolio.Type.KF;
            case SAVINGSACCOUNT:
                log.error(
                        "Normalized savings account to portfolio. This should not happen, but not failing refresh.");
                log.warn("Unknown portfolio type: {}", type);
                return Portfolio.Type.OTHER;
            default:
                log.warn("Unknown portfolio type: {}", type);
                return Portfolio.Type.OTHER;
        }
    }
}
