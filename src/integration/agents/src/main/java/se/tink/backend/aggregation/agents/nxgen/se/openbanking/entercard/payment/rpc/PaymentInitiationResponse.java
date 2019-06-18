package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.payment.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentInitiationResponse {

    private String paymentReferenceId;

    private String eSignUrl;

    private int responseCode;

    public PaymentInitiationResponse() {}

    public String getPaymentReferenceId() {
        return paymentReferenceId;
    }

    public String geteSignUrl() {
        return eSignUrl;
    }

    public int getResponseCode() {
        return responseCode;
    }
}
