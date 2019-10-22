package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PortfolioResultEntity {

    private final long balance;
    private final List<InvestmentResultEntity> investmentResultEntities;

    public PortfolioResultEntity(
            long balance, List<InvestmentResultEntity> investmentResultEntities) {
        this.balance = balance;
        this.investmentResultEntities = investmentResultEntities;
    }

    public double availableFunds() {
        return balance / RevolutConstants.REVOLUT_AMOUNT_DIVIDER;
    }

    public double totalProfit() {
        return investmentResultEntities.stream()
                .map(InvestmentResultEntity::getProfit)
                .collect(Collectors.summarizingDouble(Double::doubleValue))
                .getSum();
    }

    public double totalValue() {
        return investmentResultEntities.stream()
                        .map(InvestmentResultEntity::getTotalValue)
                        .collect(Collectors.summarizingDouble(Double::doubleValue))
                        .getSum()
                + availableFunds();
    }

    public List<InvestmentResultEntity> getInvestmentResultEntities() {
        return investmentResultEntities;
    }
}
