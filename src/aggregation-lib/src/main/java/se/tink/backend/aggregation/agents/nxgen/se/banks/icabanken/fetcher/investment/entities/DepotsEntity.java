package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.system.rpc.Portfolio;

import java.util.List;

@JsonObject
public class DepotsEntity {
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
    private String internetClientId;
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

    public double getBalance() {
        return balance;
    }

    public double getReserved() {
        return reserved;
    }

    public double getDisposable() {
        return disposable;
    }

    public double getTotalDepotValue() {
        return totalDepotValue;
    }

    public InterestsEntity getInterests() {
        return interests;
    }

    public String getInternetClientId() {
        return internetClientId;
    }

    public String getDepotNumber() {
        return depotNumber;
    }

    public String getDepotName() {
        return depotName;
    }

    public double getInvestedAmount() {
        return investedAmount;
    }

    public List<FundHoldingsEntity> getFundHoldings() {
        return fundHoldings;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public boolean isOwnerIsMinor() {
        return ownerIsMinor;
    }

    public String getOwnerCustomerId() {
        return ownerCustomerId;
    }

    public Portfolio.Type getPortfolioType(){
        switch(investmentAccountType.toLowerCase()){
            case "isk" :
                return Portfolio.Type.ISK;
            case "depot" :
                return Portfolio.Type.DEPOT;
            default:
                return Portfolio.Type.OTHER;
        }
    }
}
