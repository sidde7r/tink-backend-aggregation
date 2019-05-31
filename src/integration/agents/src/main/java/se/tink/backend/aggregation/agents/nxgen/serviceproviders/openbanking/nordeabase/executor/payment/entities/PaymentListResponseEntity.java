package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentListResponseEntity {
    List<PaymentResponseEntity> payments;

    public List<PaymentResponseEntity> getPayments() {
        return payments;
    }
}
