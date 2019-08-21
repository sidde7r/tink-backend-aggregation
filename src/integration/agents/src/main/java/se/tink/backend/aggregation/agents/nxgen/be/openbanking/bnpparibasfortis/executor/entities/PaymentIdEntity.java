package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.entities;

import org.apache.commons.lang.RandomStringUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentIdEntity {
    private String instructionId;
    private String endToEndId;

    public PaymentIdEntity() {
        instructionId = RandomStringUtils.random(35, true, true);
        endToEndId = RandomStringUtils.random(35, true, true);
    }
}
