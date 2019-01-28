package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.libraries.amount.Amount;
import se.tink.backend.aggregation.agents.models.Portfolio;

@JsonObject
public class DepositAccountEntity {
    private String id;
    private String name;
    private Double marketValue;
    private String marketValueTxt;
    private String depositAccount;
    private String accountNo;
    private String depositName;
    private Double amount;
    private String amountTxt;
    private String urlDetail;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getMarketValue() {
        return marketValue;
    }

    public String getMarketValueTxt() {
        return marketValueTxt;
    }

    public String getDepositAccount() {
        return depositAccount;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public String getDepositName() {
        return depositName;
    }

    public Double getAmount() {
        return amount;
    }

    public String getAmountTxt() {
        return amountTxt;
    }

    public String getUrlDetail() {
        return urlDetail;
    }

    private Amount toTinkAmount() {
        return Amount.inDKK(getMarketValue());
    }

    public InvestmentAccount toTinkInvestmentAccount(List<Portfolio> portfolios) {
        return InvestmentAccount.builder(getId())
                .setCashBalance(Amount.inDKK(0))
                .setAccountNumber(getAccountNo())
                .setBankIdentifier(getId())
                .setName(getName())
                .setPortfolios(portfolios)
                .build();
    }
}
