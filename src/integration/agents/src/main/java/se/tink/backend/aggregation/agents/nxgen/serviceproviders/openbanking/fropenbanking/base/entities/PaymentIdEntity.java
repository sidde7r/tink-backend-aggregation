package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities;

import lombok.Getter;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class PaymentIdEntity {
    private String instructionId;
    private String endToEndId;

    public PaymentIdEntity() {
        this.instructionId = RandomUtils.generateRandomAlphanumericString(35);
        this.endToEndId = RandomUtils.generateRandomAlphanumericString(35);
    }
}
