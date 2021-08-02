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
public class PaymentTypeInformationEntity {
    @JsonProperty("instructionPriority")
    private PriorityCodeEntity instructionPriority;

    @JsonProperty("serviceLevel")
    private ServiceLevelCodeEntity serviceLevel;

    @JsonProperty("localInstrument")
    private String localInstrument;

    @JsonProperty("categoryPurpose")
    private CategoryPurposeCodeEntity categoryPurpose;

    @JsonCreator
    public PaymentTypeInformationEntity(
            PriorityCodeEntity instructionPriority,
            ServiceLevelCodeEntity serviceLevel,
            String localInstrument,
            CategoryPurposeCodeEntity categoryPurpose) {
        this.instructionPriority = instructionPriority;
        this.serviceLevel = serviceLevel;
        this.localInstrument = localInstrument;
        this.categoryPurpose = categoryPurpose;
    }
}
