package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentAuthorizationStatus {
    @JsonProperty private String scaStatus;

    public String getScaStatus() {
        return scaStatus;
    }

    public void setScaStatus(String scaStatus) {
        this.scaStatus = scaStatus;
    }
}
