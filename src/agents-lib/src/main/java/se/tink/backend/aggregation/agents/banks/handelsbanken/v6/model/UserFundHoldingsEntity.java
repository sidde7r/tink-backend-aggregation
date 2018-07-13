package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserFundHoldingsEntity {
    private String ownerName;
    private PartEntity part;
    private boolean hasWithdrawal;
    private FundHoldingSummaryEntity fundHoldingSummary;
    private List<FundHoldingsEntity> fundHoldingList;
    private boolean minor;

    public FundHoldingSummaryEntity getFundHoldingSummary() {
        return fundHoldingSummary;
    }

    public void setFundHoldingSummary(FundHoldingSummaryEntity fundHoldingSummary) {
        this.fundHoldingSummary = fundHoldingSummary;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public PartEntity getPart() {
        return part;
    }

    public void setPart(PartEntity part) {
        this.part = part;
    }

    public boolean isHasWithdrawal() {
        return hasWithdrawal;
    }

    public void setHasWithdrawal(boolean hasWithdrawal) {
        this.hasWithdrawal = hasWithdrawal;
    }

    public List<FundHoldingsEntity> getFundHoldingList() {
        return fundHoldingList;
    }

    public void setFundHoldingList(
            List<FundHoldingsEntity> fundHoldingList) {
        this.fundHoldingList = fundHoldingList;
    }

    public boolean isMinor() {
        return minor;
    }

    public void setMinor(boolean minor) {
        this.minor = minor;
    }
}
