package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.executor.payment.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentResponseLink {
    private String scaRedirect;

    public String getScaRedirect() {
        return scaRedirect;
    }

    public void setScaRedirect(String scaRedirect) {
        this.scaRedirect = scaRedirect;
    }
}
