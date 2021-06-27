package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentOrders {
    private String paymentUid;
    private AmountEntity amount;
    private String reference;
    private String payeeUid;
    private String payeeAccountUid;
    private String createdAt;
    private String completedAt;
    private String rejectedAt;
    private PaymentStatusDetails paymentStatusDetails;

    public String getPaymentUid() {
        return this.paymentUid;
    }

    public PaymentStatusDetails getPaymentStatusDetails() {
        return this.paymentStatusDetails;
    }
}
