package se.tink.backend.core;

import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import java.io.Serializable;

@Table(value = "bank_fee_statistics")
public class BankFeeStatistics implements Serializable {

    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String providerName;
    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private Integer year;
    @PrimaryKeyColumn(ordinal = 2, type = PrimaryKeyType.CLUSTERED)
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
