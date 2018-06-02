package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TransfersConfiguration {

    @JsonProperty
    private int bankTransferThreshold;
    @JsonProperty
    private boolean enabled;
    @JsonProperty
    private int paymentThreshold;
    @JsonProperty
    private int duplicateTime;
    @JsonProperty
    private int aggregationTime;

    public int getBankTransferThreshold() {
        return bankTransferThreshold;
    }

    public int getPaymentThreshold() {
        return paymentThreshold;
    }


    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getDuplicateTime() {
        return duplicateTime;
    }

    public int getAggregationTime() {
        return aggregationTime;
    }

}
