package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment.entity;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class PaymentStatusDetails {
    String paymentStatus;
    String description;
}
