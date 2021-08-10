package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class PaymentIdentificationEntity {
    @JsonProperty("resourceId")
    private String resourceId;

    @JsonProperty("instructionId")
    private String instructionId;

    @JsonProperty("endToEndId")
    private String endToEndId;

    @JsonCreator
    public PaymentIdentificationEntity(String resourceId, String instructionId, String endToEndId) {
        this.resourceId = resourceId;
        this.instructionId = instructionId;
        this.endToEndId = endToEndId;
    }
}
