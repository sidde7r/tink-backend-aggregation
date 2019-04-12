package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class PaymentListResponseEntity {
    List<PaymentResponseEntity> payments;

    public List<PaymentResponseEntity> getPayments() {
        return payments;
    }
}
