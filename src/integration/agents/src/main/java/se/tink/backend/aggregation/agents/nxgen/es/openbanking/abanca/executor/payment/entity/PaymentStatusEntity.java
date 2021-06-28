package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.executor.payment.entity;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class PaymentStatusEntity {

    private String id;
    private String type;
    private PaymentStatusAttributesEntity attributes;
    private LinksEntity links;
}
