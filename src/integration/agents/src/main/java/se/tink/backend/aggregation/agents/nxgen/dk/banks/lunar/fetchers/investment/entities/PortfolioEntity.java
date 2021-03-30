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
    private Double cashBalance;
    private String currency;
    private String id;
    private BigDecimal totalValue;
    private BigDecimal totalOpenPositionsValue;

    @JsonIgnore
    public List<InvestmentAccount> toInvestmentAccounts(
            PerformanceDataEntity performanceData,
            List<InstrumentEntity> instruments,
            String holderName) {
        return Collections.singletonList(
                InvestmentAccount.nxBuilder()
                        .withPortfolios(buildPortfolioModule(performanceData, instruments))
                        .withCashBalance(
                                ExactCurrencyAmount.of(calculateCashBalanceRest(), currency))
                        .withId(buildIdModule())
                        .addHolderName(holderName)
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
        return PortfolioModule.builder()
                .withType(PortfolioModule.PortfolioType.DEPOT)
                .withUniqueIdentifier(accountNumber)
                .withCashValue(cashBalance != null ? cashBalance : 0)
                .withTotalProfit(getTotalProfit(performanceData))
                .withTotalValue(getPortfolioTotalValue().doubleValue())
                .withInstruments(buildInstruments(instruments))
                .build();
    }

    private double getTotalProfit(PerformanceDataEntity performanceData) {
        return performanceData != null ? performanceData.getTotalProfit() : 0;
    }

    private BigDecimal getPortfolioTotalValue() {
        if (totalOpenPositionsValue != null) {
            return totalOpenPositionsValue;
        }
        return totalValue != null ? totalValue : BigDecimal.ZERO;
    }

    private double calculateCashBalanceRest() {
        // Investment cash balance is a sum of all portfolios and cash balance
        return totalValue != null ? totalValue.subtract(getPortfolioTotalValue()).doubleValue() : 0;
    }

    private List<InstrumentModule> buildInstruments(List<InstrumentEntity> instruments) {
        return instruments.stream()
                .map(InstrumentEntity::toInstrument)
                .collect(Collectors.toList());
    }
}
