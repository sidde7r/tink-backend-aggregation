package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class InvestmentAccountEntity {
    private String accountNumber;

    @JsonProperty("has_additional_info")
    private boolean hasMoreInfo;

    private double profitLoss;
    private boolean profitLossValid;

    @JsonProperty("cash_amount")
    private double balance;

    private String name;
    private String id;
    private String classification;
    private String currency;

    @JsonProperty("market_value")
    private double value;

    private List<HoldingEntity> holdings;

    public InvestmentAccount toTinkInvestmentAccount() {
        return InvestmentAccount.builder(id)
                .setCashBalance(ExactCurrencyAmount.of(balance, currency))
                .setBankIdentifier(id)
                .setAccountNumber(accountNumber)
                .setName(name)
                .setPortfolios(Collections.singletonList(getTinkPortfolio()))
                .build();
    }

    private Portfolio getTinkPortfolio() {
        Portfolio portfolio = new Portfolio();
        portfolio.setUniqueIdentifier(id);
        portfolio.setRawType(getRawType());
        portfolio.setType(
                NordeaFIConstants.PORTFOLIO_TYPE_MAP
                        .translate(getRawType())
                        .orElse(Portfolio.Type.OTHER));
        portfolio.setCashValue(balance);
        portfolio.setTotalValue(balance);
        portfolio.setTotalProfit(profitLoss);
        portfolio.setInstruments(getInstruments());

        return portfolio;
    }

    private List<Instrument> getInstruments() {
        if (holdings != null) {
            return holdings.stream()
                    .filter(HoldingEntity::isInstrument)
                    .map(HoldingEntity::toTinkInstrument)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private String getRawType() {
        // id format: TYPE:ACCOUNT
        return id.split(":")[0];
    }

    public boolean hasHoldings() {
        return holdings != null && !holdings.isEmpty();
    }
}
