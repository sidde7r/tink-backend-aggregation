package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class PaymentIdentificationEntity {
    @JsonProperty("resourceId")
    private String resourceId = null;

    @JsonProperty("instructionId")
    private String instructionId = null;

    @JsonProperty("endToEndId")
    private String endToEndId = null;

    @JsonCreator
    public PaymentIdentificationEntity(String resourceId, String instructionId, String endToEndId) {
        this.resourceId = resourceId;
        this.instructionId = instructionId;
        this.endToEndId = endToEndId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getInstructionId() {
        return instructionId;
    }

    public void setInstructionId(String instructionId) {
        this.instructionId = instructionId;
    }

    public String getEndToEndId() {
        return endToEndId;
    }

    public void setEndToEndId(String endToEndId) {
        this.endToEndId = endToEndId;
    }
}
