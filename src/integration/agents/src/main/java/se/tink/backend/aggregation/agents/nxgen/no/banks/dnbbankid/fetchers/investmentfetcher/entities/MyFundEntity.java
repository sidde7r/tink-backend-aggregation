package se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.fetchers.investmentfetcher.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MyFundEntity {
    private String name;
    private double sum;
    private double returnSum;
    private String productSystem;
    private String productId;
    private double shares;
    private String holdingType;
    private String owner;
    private double price;
    private double costPrice;
    private boolean fundSuspended;
    private boolean fundSuspendedBuy;
    private boolean fundSuspendedSell;
    private boolean accountSuspended;
    private double amountAvailable;
    private boolean amountAvailableInProcess;
    private boolean allowedToPurchaseMore;
    private boolean allowedToSell;
    private boolean allowedToSwitch;
    private double returnOfInvestmentPercentage;
    private List<FundAccountInMyFundEntity> fundAccounts;
    private boolean ips;
    private boolean onlyIPS;

    // Note: not from response, set manually.
    private String isin;

    public String getName() {
        return name;
    }

    public double getSum() {
        return sum;
    }

    public double getReturnSum() {
        return returnSum;
    }

    public String getProductSystem() {
        return productSystem;
    }

    public String getProductId() {
        return productId;
    }

    public double getShares() {
        return shares;
    }

    public String getHoldingType() {
        return holdingType;
    }

    public String getOwner() {
        return owner;
    }

    public double getPrice() {
        return price;
    }

    public double getCostPrice() {
        return costPrice;
    }

    public boolean isFundSuspended() {
        return fundSuspended;
    }

    public boolean isFundSuspendedBuy() {
        return fundSuspendedBuy;
    }

    public boolean isFundSuspendedSell() {
        return fundSuspendedSell;
    }

    public boolean isAccountSuspended() {
        return accountSuspended;
    }

    public double getAmountAvailable() {
        return amountAvailable;
    }

    public boolean isAmountAvailableInProcess() {
        return amountAvailableInProcess;
    }

    public boolean isAllowedToPurchaseMore() {
        return allowedToPurchaseMore;
    }

    public boolean isAllowedToSell() {
        return allowedToSell;
    }

    public boolean isAllowedToSwitch() {
        return allowedToSwitch;
    }

    public double getReturnOfInvestmentPercentage() {
        return returnOfInvestmentPercentage;
    }

    public List<FundAccountInMyFundEntity> getFundAccounts() {
        return fundAccounts;
    }

    public boolean isIps() {
        return ips;
    }

    public boolean isOnlyIPS() {
        return onlyIPS;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }
}
