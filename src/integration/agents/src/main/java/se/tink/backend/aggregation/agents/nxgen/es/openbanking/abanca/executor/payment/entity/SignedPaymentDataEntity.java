package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.executor.payment.entity;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class SignedPaymentDataEntity {

    private String id;
    private String type;
    private SignedPaymentAttributesEntity attributes;
    private LinksEntity links;
}
