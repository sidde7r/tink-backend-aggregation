package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.Portfolio;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DepotEntity {
    @JsonProperty("InvestmentAccountType")
    private String investmentAccountType;
    @JsonProperty("Balance")
    private double balance;
    @JsonProperty("Reserved")
    private double reserved;
    @JsonProperty("Disposable")
    private double disposable;
    @JsonProperty("TotalDepotValue")
    private double totalDepotValue;
    @JsonProperty("Interests")
    private InterestsEntity interests;
    @JsonProperty("InternetClientId")
    private String clientId;
    @JsonProperty("DepotNumber")
    private String depotNumber;
    @JsonProperty("DepotName")
    private String depotName;
    @JsonProperty("InvestedAmount")
    private double investedAmount;
    @JsonProperty("FundHoldings")
    private List<FundHoldingsEntity> fundHoldings;
    @JsonProperty("IsLocked")
    private boolean isLocked;
    @JsonProperty("OwnerIsMinor")
    private boolean ownerIsMinor;
    @JsonProperty("OwnerCustomerId")
    private String ownerCustomerId;

    public String getInvestmentAccountType() {
        return investmentAccountType;
    }

    public void setInvestmentAccountType(String investmentAccountType) {
        this.investmentAccountType = investmentAccountType;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getReserved() {
        return reserved;
    }

    public void setReserved(double reserved) {
        this.reserved = reserved;
    }

    public double getDisposable() {
        return disposable;
    }

    public void setDisposable(double disposable) {
        this.disposable = disposable;
    }

    public double getTotalDepotValue() {
        return totalDepotValue;
    }

    public void setTotalDepotValue(double totalDepotValue) {
        this.totalDepotValue = totalDepotValue;
    }

    public InterestsEntity getInterests() {
        return interests;
    }

    public void setInterests(InterestsEntity interests) {
        this.interests = interests;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getDepotNumber() {
        return depotNumber;
    }

    public void setDepotNumber(String depotNumber) {
        this.depotNumber = depotNumber;
    }

    public String getDepotName() {
        return depotName;
    }

    public void setDepotName(String depotName) {
        this.depotName = depotName;
    }

    public double getInvestedAmount() {
        return investedAmount;
    }

    public void setInvestedAmount(double investedAmount) {
        this.investedAmount = investedAmount;
    }

    public List<FundHoldingsEntity> getFundHoldings() {
        return fundHoldings;
    }

    public void setFundHoldings(List<FundHoldingsEntity> fundHoldings) {
        this.fundHoldings = fundHoldings;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public boolean isOwnerIsMinor() {
        return ownerIsMinor;
    }

    public void setOwnerIsMinor(boolean ownerIsMinor) {
        this.ownerIsMinor = ownerIsMinor;
    }

    public String getOwnerCustomerId() {
        return ownerCustomerId;
    }

    public void setOwnerCustomerId(String ownerCustomerId) {
        this.ownerCustomerId = ownerCustomerId;
    }

    public Account toAccount() {
        Account account = new Account();
        account.setAccountNumber(getDepotNumber());
        account.setBalance(getTotalDepotValue());
        account.setBankId(getDepotNumber());
        account.setName(getDepotName());
        account.setType(AccountTypes.INVESTMENT);

        return account;
    }

    public Portfolio toPortfolio() {
        Portfolio portfolio = new Portfolio();
        portfolio.setCashValue(getDisposable());
        portfolio.setTotalValue(getInvestedAmount());
        portfolio.setUniqueIdentifier(getDepotNumber());
        portfolio.setType(getPortfolioType());
        portfolio.setRawType(getInvestmentAccountType());
        portfolio.setTotalProfit(getTotalDepotValue() - getInvestedAmount());

        return portfolio;
    }

    private Portfolio.Type getPortfolioType() {
        switch (getInvestmentAccountType().toLowerCase()) {
        case "isk":
            return Portfolio.Type.ISK;
        case "depot":
            return Portfolio.Type.DEPOT;
        default:
            return Portfolio.Type.OTHER;
        }
    }
}
