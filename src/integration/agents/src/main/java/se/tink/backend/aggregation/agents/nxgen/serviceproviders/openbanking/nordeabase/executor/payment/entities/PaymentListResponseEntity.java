package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class PaymentListResponseEntity {
    List<PaymentResponseEntity> payments;

    public List<PaymentResponseEntity> getPayments() {
        return payments;
    }
}
