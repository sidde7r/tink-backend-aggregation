package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

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

    public PaymentIdentificationEntity() {}

    private PaymentIdentificationEntity(
            String resourceId, String instructionId, String endToEndId) {
        this.resourceId = resourceId;
        this.instructionId = instructionId;
        this.endToEndId = endToEndId;
    }

    public static PaymentIdentificationEntityBuilder builder() {
        return new PaymentIdentificationEntityBuilder();
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

    public static class PaymentIdentificationEntityBuilder {

        private String resourceId;
        private String instructionId;
        private String endToEndId;

        PaymentIdentificationEntityBuilder() {}

        public PaymentIdentificationEntityBuilder resourceId(String resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public PaymentIdentificationEntityBuilder instructionId(String instructionId) {
            this.instructionId = instructionId;
            return this;
        }

        public PaymentIdentificationEntityBuilder endToEndId(String endToEndId) {
            this.endToEndId = endToEndId;
            return this;
        }

        public PaymentIdentificationEntity build() {
            return new PaymentIdentificationEntity(resourceId, instructionId, endToEndId);
        }

        public String toString() {
            return "PaymentIdentificationEntity.PaymentIdentificationEntityBuilder(resourceId="
                    + this.resourceId
                    + ", instructionId="
                    + this.instructionId
                    + ", endToEndId="
                    + this.endToEndId
                    + ")";
        }
    }
}
