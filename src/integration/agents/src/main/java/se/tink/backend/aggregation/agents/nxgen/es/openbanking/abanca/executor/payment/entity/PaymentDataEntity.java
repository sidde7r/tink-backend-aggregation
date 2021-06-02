package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.executor.payment.entity;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@AllArgsConstructor
@JsonObject
public class PaymentDataEntity {

    private final String type;
    private final PaymentAttributesEntity attributes;
}
