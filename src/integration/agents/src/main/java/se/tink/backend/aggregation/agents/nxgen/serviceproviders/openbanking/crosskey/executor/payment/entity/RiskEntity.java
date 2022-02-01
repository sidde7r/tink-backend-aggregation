package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.executor.payment.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.UpperCamelCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@Setter
@JsonNaming(UpperCamelCaseStrategy.class)
public class RiskEntity {

    private String paymentContextCode;

    public RiskEntity(String paymentContextCode) {
        this.paymentContextCode = paymentContextCode;
    }

    public RiskEntity() {}
}
