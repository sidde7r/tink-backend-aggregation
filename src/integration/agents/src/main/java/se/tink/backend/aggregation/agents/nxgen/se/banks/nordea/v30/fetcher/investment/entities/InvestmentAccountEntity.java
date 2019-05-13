package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.amount.Amount;

@JsonObject
public class InvestmentAccountEntity {

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("has_additional_info")
    private boolean hasMoreInfo;

    @JsonProperty("profit_loss")
    private double profitLoss;

    @JsonProperty("profit_loss_valid")
    private boolean profitLossValid;

    @JsonProperty("cash_amount")
    private double balance;

    @JsonProperty private String name;
    @JsonProperty private String id;
    @JsonProperty private String classification;

    @JsonProperty("market_value")
    private double value;

    @JsonProperty private List<HoldingEntity> holdings;

    public InvestmentAccount toTinkInvestmentAccount() {

        // Temporary solution since pension accounts does not have any holdings
        // TODO: create pension account builder
        if (classification.equalsIgnoreCase("PENSION")) {
            return InvestmentAccount.builder(id)
                    .setBalance(new Amount(NordeaSEConstants.CURRENCY, value))
                    .setBankIdentifier(id)
                    .setAccountNumber(accountNumber)
                    .setName(name)
                    .build();
        }

        return InvestmentAccount.builder(id)
                .setCashBalance(new Amount(NordeaSEConstants.CURRENCY, balance))
                .setBankIdentifier(id)
                .setAccountNumber(accountNumber)
                .setName(name)
                .setPortfolios(Collections.singletonList(getTinkPortfolio()))
                .build();
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setHoldings(List<HoldingEntity> holdings) {
        this.holdings = holdings;
    }

    public List<HoldingEntity> getHoldings() {
        return holdings;
    }

    private Portfolio getTinkPortfolio() {

        Portfolio portfolio = new Portfolio();
        portfolio.setUniqueIdentifier(id);
        portfolio.setRawType(getRawType());
        portfolio.setType(
                NordeaSEConstants.PORTFOLIO_TYPE_MAP
                        .translate(getRawType())
                        .orElse(Portfolio.Type.OTHER));
        portfolio.setCashValue(balance);
        portfolio.setTotalValue(balance + value);
        portfolio.setTotalProfit(profitLoss);
        portfolio.setInstruments(getInstruments());

        return portfolio;
    }

    private List<Instrument> getInstruments() {
        return getHoldings().stream()
                .filter(HoldingEntity::isInstrument)
                .map(HoldingEntity::toTinkInstrument)
                .collect(Collectors.toList());
    }

    private String getRawType() {

        // id format: TYPE:ACCOUNT
        return id.split(":")[0];
    }
}
