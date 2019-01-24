package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.core.Amount;
import se.tink.backend.aggregation.agents.models.Portfolio;

@JsonObject
public class BankdataDepositEntity {
    private String regNo;
    private String depositNo;
    private String name;
    private double quotedValue;
    private boolean tradesAllowed;
    private String depositOwner;
    private double returns;
    private boolean pensionDeposit;
    private boolean ownDeposit;

    public String getRegNo() {
        return regNo;
    }

    public String getDepositNo() {
        return depositNo;
    }

    public String getName() {
        return name;
    }

    public double getQuotedValue() {
        return quotedValue;
    }

    public boolean isTradesAllowed() {
        return tradesAllowed;
    }

    public String getDepositOwner() {
        return depositOwner;
    }

    public double getReturns() {
        return returns;
    }

    public boolean isPensionDeposit() {
        return pensionDeposit;
    }

    public boolean isOwnDeposit() {
        return ownDeposit;
    }

    public Amount toTinkAmount() {
        return Amount.inDKK(quotedValue);
    }

    public double getMarketValue() {
        return quotedValue;
    }

    public String getAccountNumberFormatted() {
        return String.format("%s-%s", regNo, depositNo);
    }

    public String getAccountNumber() {
        return regNo + depositNo;
    }

    public Portfolio toTinkPortfolio() {
        Portfolio portfolio = new Portfolio();
        portfolio.setUniqueIdentifier(getAccountNumber());
        portfolio.setTotalValue(getMarketValue());
        portfolio.setTotalProfit(getReturns());
        portfolio.setType(Portfolio.Type.DEPOT);
        return portfolio;
    }

}
