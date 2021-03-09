package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.libraries.account.identifiers.DanishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Slf4j
public class PortfolioEntity {
    @Getter private String accountNumber;
    private BigDecimal cashBalance;
    private String currency;
    private String id;
    private BigDecimal totalValue;
    private BigDecimal totalOpenPositionsValue;

    @JsonIgnore
    public List<InvestmentAccount> toInvestmentAccounts(
            PerformanceDataEntity performanceData, List<InstrumentEntity> instruments) {
        return Collections.singletonList(
                InvestmentAccount.nxBuilder()
                        .withPortfolios(buildPortfolioModule(performanceData, instruments))
                        .withCashBalance(
                                ExactCurrencyAmount.of(
                                        calculateCashBalanceRest(instruments), currency))
                        .withId(buildIdModule())
                        .build());
    }

    private IdModule buildIdModule() {
        return IdModule.builder()
                .withUniqueIdentifier(accountNumber)
                .withAccountNumber(accountNumber)
                .withAccountName(accountNumber)
                .addIdentifier(new DanishIdentifier(accountNumber))
                .build();
    }

    private PortfolioModule buildPortfolioModule(
            PerformanceDataEntity performanceData, List<InstrumentEntity> instruments) {
        // Wiski delete unnecessary logs
        if (totalValue == null && !instruments.isEmpty()) {
            log.info("Lunar user has no investment funds but owns some instruments!");
        }

        return PortfolioModule.builder()
                .withType(PortfolioModule.PortfolioType.DEPOT)
                .withUniqueIdentifier(accountNumber)
                .withCashValue(cashBalance != null ? cashBalance.doubleValue() : 0)
                .withTotalProfit(getTotalProfit(performanceData))
                .withTotalValue(calculateTotalValue(instruments).doubleValue())
                .withInstruments(buildInstruments(instruments))
                .build();
    }

    private double getTotalProfit(PerformanceDataEntity performanceData) {
        if (performanceData == null || totalValue == null) {
            return 0;
        }
        BigDecimal currentAvailablePositionsValues =
                totalOpenPositionsValue != null
                        ? totalOpenPositionsValue.add(cashBalance)
                        : totalValue;
        return performanceData.getTotalProfit(currentAvailablePositionsValues);
    }

    private BigDecimal calculateTotalValue(List<InstrumentEntity> instruments) {
        BigDecimal calculatedValue = cashBalance != null ? cashBalance : BigDecimal.ZERO;
        for (InstrumentEntity instrument : instruments) {
            calculatedValue = calculatedValue.add(instrument.calculateInstrumentValue());
        }
        return calculatedValue;
    }

    private double calculateCashBalanceRest(List<InstrumentEntity> instruments) {
        // Investment cash balance is a sum of all portfolios and cash balance
        return totalValue != null
                ? totalValue.subtract(calculateTotalValue(instruments)).doubleValue()
                : 0;
    }

    private List<InstrumentModule> buildInstruments(List<InstrumentEntity> instruments) {
        return instruments.stream()
                .map(InstrumentEntity::toInstrument)
                .collect(Collectors.toList());
    }
}
