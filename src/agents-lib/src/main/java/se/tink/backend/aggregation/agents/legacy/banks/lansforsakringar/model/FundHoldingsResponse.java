package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.Portfolio;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FundHoldingsResponse {
    // The response does not contain any account number or similar
    @JsonIgnore
    private static final String IDENTIFIER = "funddepot";
    @JsonIgnore
    private static final String NAME = "Fonddepå";
    private Double totalHoldingValue;
    private Double totalPurchaseValue;
    private Double totalDevelopment;
    private List<FundEntity> funds;

    public Double getTotalHoldingValue() {
        return totalHoldingValue;
    }

    public void setTotalHoldingValue(Double totalHoldingValue) {
        this.totalHoldingValue = totalHoldingValue;
    }

    public Double getTotalPurchaseValue() {
        return totalPurchaseValue;
    }

    public void setTotalPurchaseValue(Double totalPurchaseValue) {
        this.totalPurchaseValue = totalPurchaseValue;
    }

    public Double getTotalDevelopment() {
        return totalDevelopment;
    }

    public void setTotalDevelopment(Double totalDevelopment) {
        this.totalDevelopment = totalDevelopment;
    }

    public List<FundEntity> getFunds() {
        return funds;
    }

    public void setFunds(List<FundEntity> funds) {
        this.funds = funds;
    }

    public Account toAccount() {
        Account account = new Account();

        account.setAccountNumber(""); // LF don't provide an account number
        account.setBankId(IDENTIFIER); // LF don't provide a field for this
        account.setBalance(getTotalHoldingValue());
        account.setName(NAME); // LF don't provide a name field
        account.setType(AccountTypes.INVESTMENT);

        return account;
    }

    public Portfolio toPortfolio() {
        Portfolio portfolio = new Portfolio();

        portfolio.setType(Portfolio.Type.DEPOT);
        portfolio.setTotalProfit(getTotalDevelopment());
        portfolio.setTotalValue(getTotalHoldingValue());
        portfolio.setUniqueIdentifier(IDENTIFIER); // LF don't provide a field for this

        return portfolio;
    }
}
