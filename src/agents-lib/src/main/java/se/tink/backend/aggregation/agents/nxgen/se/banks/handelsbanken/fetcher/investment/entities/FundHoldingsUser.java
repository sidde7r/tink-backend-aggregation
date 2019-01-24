package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.core.Amount;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;

@JsonObject
public class FundHoldingsUser {

    private FundHoldingPart part;
    private FundHoldingSummary fundHoldingSummary;
    private List<FundHolding> fundHoldingList;

    public String getIdentifier() {
        return part != null ? part.getIdentifier() : null;
    }

    private double toSummaryMarketValue() {
        return fundHoldingSummary != null ? fundHoldingSummary.getMarketValue() : 0;
    }

    private double toSummaryPurchaseValue() {
        return fundHoldingSummary != null ? fundHoldingSummary.getPurchaseValue() : 0;
    }

    public Portfolio applyTo(Portfolio portfolio) {
        double summaryMarketValue = toSummaryMarketValue();
        portfolio.setTotalValue(summaryMarketValue);
        portfolio.setTotalProfit(summaryMarketValue - toSummaryPurchaseValue());

        portfolio.setUniqueIdentifier(getIdentifier());

        portfolio.setInstruments(toInstruments());
        return portfolio;
    }

    private List<Instrument> toInstruments() {
        if (fundHoldingList == null) {
            return Collections.emptyList();
        }
        return fundHoldingList.stream().map(FundHolding::toInstrument)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public InvestmentAccount toAccount(CustodyAccount custodyAccount) {
        return InvestmentAccount.builder(getIdentifier())
                .setAccountNumber(getIdentifier())
                .setName(custodyAccount.getTitle())
                .setCashBalance(Amount.inSEK(0))
                .setPortfolios(Collections.singletonList(toPortfolio(custodyAccount)))
                .build();
    }

    private Portfolio toPortfolio(CustodyAccount custodyAccount) {
        Portfolio portfolio = new Portfolio();
        portfolio.setType(Portfolio.Type.DEPOT);
        portfolio.setRawType(custodyAccount.getType());
        return applyTo(portfolio);
    }
}
