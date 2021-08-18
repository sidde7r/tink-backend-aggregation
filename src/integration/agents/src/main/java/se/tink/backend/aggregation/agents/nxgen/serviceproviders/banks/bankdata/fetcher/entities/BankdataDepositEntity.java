package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class BankdataDepositEntity {

    private String regNo;
    private String depositNo;
    private String name;

    @JsonProperty("quotedValue")
    private double marketValue;

    private boolean tradesAllowed;
    private String depositOwner;
    private double returns;
    private boolean pensionDeposit;
    private boolean ownDeposit;

    public String getBban() {
        return regNo + depositNo;
    }

    public Portfolio toTinkPortfolio() {
        Portfolio portfolio = new Portfolio();
        portfolio.setUniqueIdentifier(getBban());
        portfolio.setTotalValue(getMarketValue());
        portfolio.setTotalProfit(getReturns());
        portfolio.setType(Portfolio.Type.DEPOT);
        return portfolio;
    }
}
