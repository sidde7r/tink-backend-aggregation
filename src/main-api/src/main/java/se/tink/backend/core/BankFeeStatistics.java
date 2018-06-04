package se.tink.backend.core;

import java.io.Serializable;

public class BankFeeStatistics implements Serializable {

    private String providerName;
    private Integer year;
    private String type;

    private Double averageAmount;

    private String serializedDetails;

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public BankFeeType getType() {
        return BankFeeType.valueOf(type);
    }

    public void setType(BankFeeType type) {
        this.type = type.toString();
    }

    public String getSerializedDetails() {
        return serializedDetails;
    }

    public void setSerializedDetails(String serializedDetails) {
        this.serializedDetails = serializedDetails;
    }

    public Double getAverageAmount() {
        return averageAmount;
    }

    public void setAverageAmount(Double averageAmount) {
        this.averageAmount = averageAmount;
    }
}
