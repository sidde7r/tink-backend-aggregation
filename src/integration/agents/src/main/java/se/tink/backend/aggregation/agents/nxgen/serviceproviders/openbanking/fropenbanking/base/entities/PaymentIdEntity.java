package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities;

import lombok.Getter;
import org.apache.commons.lang.RandomStringUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class PaymentIdEntity {
    private String instructionId;
    private String endToEndId;
    private String resourceId;

    public PaymentIdEntity() {
        this.instructionId = RandomStringUtils.random(35, true, true);
        this.endToEndId = RandomStringUtils.random(35, true, true);
        this.resourceId = RandomStringUtils.random(35, true, true);
    }
}
