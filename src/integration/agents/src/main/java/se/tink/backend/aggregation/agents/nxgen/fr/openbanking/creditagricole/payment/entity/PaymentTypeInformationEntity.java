package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

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

    public PaymentTypeInformationEntity() {}

    private PaymentTypeInformationEntity(
            PriorityCodeEntity instructionPriority,
            ServiceLevelCodeEntity serviceLevel,
            String localInstrument,
            CategoryPurposeCodeEntity categoryPurpose) {
        this.instructionPriority = instructionPriority;
        this.serviceLevel = serviceLevel;
        this.localInstrument = localInstrument;
        this.categoryPurpose = categoryPurpose;
    }

    public static PaymentTypeInformationEntityBuilder builder() {
        return new PaymentTypeInformationEntityBuilder();
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

    public static class PaymentTypeInformationEntityBuilder {

        private PriorityCodeEntity instructionPriority;
        private ServiceLevelCodeEntity serviceLevel;
        private String localInstrument;
        private CategoryPurposeCodeEntity categoryPurpose;

        PaymentTypeInformationEntityBuilder() {}

        public PaymentTypeInformationEntityBuilder instructionPriority(
                PriorityCodeEntity instructionPriority) {
            this.instructionPriority = instructionPriority;
            return this;
        }

        public PaymentTypeInformationEntityBuilder serviceLevel(
                ServiceLevelCodeEntity serviceLevel) {
            this.serviceLevel = serviceLevel;
            return this;
        }

        public PaymentTypeInformationEntityBuilder localInstrument(String localInstrument) {
            this.localInstrument = localInstrument;
            return this;
        }

        public PaymentTypeInformationEntityBuilder categoryPurpose(
                CategoryPurposeCodeEntity categoryPurpose) {
            this.categoryPurpose = categoryPurpose;
            return this;
        }

        public PaymentTypeInformationEntity build() {
            return new PaymentTypeInformationEntity(
                    instructionPriority, serviceLevel, localInstrument, categoryPurpose);
        }

        public String toString() {
            return "PaymentTypeInformationEntity.PaymentTypeInformationEntityBuilder(instructionPriority="
                    + this.instructionPriority
                    + ", serviceLevel="
                    + this.serviceLevel
                    + ", localInstrument="
                    + this.localInstrument
                    + ", categoryPurpose="
                    + this.categoryPurpose
                    + ")";
        }
    }
}
