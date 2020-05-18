package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;


@JsonObject
public class PaymentTypeInformationEntity {
    private String serviceLevel;
    private String localInstrument;
    private String categoryPurpose;


    @JsonIgnore
    private PaymentTypeInformationEntity(Builder builder) {
        this.serviceLevel = builder.serviceLevel;
        this.localInstrument = builder.localInstrument;
        this.categoryPurpose = builder.categoryPurpose;
    }

    public PaymentTypeInformationEntity() {}

    public static class Builder {
        private String serviceLevel;
        private String localInstrument;
        private String categoryPurpose;

        public Builder withCategoryPurpose(String categoryPurpose) {
            this.categoryPurpose = categoryPurpose;
            return this;
        }

        public Builder withServiceLevel(String serviceLevel) {
            this.serviceLevel = serviceLevel;
            return this;
        }

        public Builder withLocalInstrument(String localInstrument) {
            this.localInstrument = localInstrument;
            return this;
        }

        public PaymentTypeInformationEntity build() {
            return new PaymentTypeInformationEntity(this);
        }
    }
}
