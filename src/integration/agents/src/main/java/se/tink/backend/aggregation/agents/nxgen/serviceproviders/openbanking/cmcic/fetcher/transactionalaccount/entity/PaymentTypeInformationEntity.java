package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class PaymentTypeInformationEntity {
    @JsonProperty("instructionPriority")
    private PriorityCodeEntity instructionPriority = null;

    @JsonProperty("serviceLevel")
    private ServiceLevelCodeEntity serviceLevel = null;

    @JsonProperty("localInstrument")
    private String localInstrument = null;

    @JsonProperty("categoryPurpose")
    private CategoryPurposeCodeEntity categoryPurpose = null;

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

    public PriorityCodeEntity getInstructionPriority() {
        return instructionPriority;
    }

    public void setInstructionPriority(PriorityCodeEntity instructionPriority) {
        this.instructionPriority = instructionPriority;
    }

    public ServiceLevelCodeEntity getServiceLevel() {
        return serviceLevel;
    }

    public void setServiceLevel(ServiceLevelCodeEntity serviceLevel) {
        this.serviceLevel = serviceLevel;
    }

    public String getLocalInstrument() {
        return localInstrument;
    }

    public void setLocalInstrument(String localInstrument) {
        this.localInstrument = localInstrument;
    }

    public CategoryPurposeCodeEntity getCategoryPurpose() {
        return categoryPurpose;
    }

    public void setCategoryPurpose(CategoryPurposeCodeEntity categoryPurpose) {
        this.categoryPurpose = categoryPurpose;
    }
}
