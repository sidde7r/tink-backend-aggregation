package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.entities;

import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants.PaymentTypeInformation;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

@JsonObject
public class PaymentTypeInformationEntity {
    private String serviceLevel;
    private String localInstrument;
    private String categoryPurpose;

    public static PaymentTypeInformationEntity of(PaymentRequest paymentRequest) {
        return new PaymentTypeInformationEntity.Builder()
                .withCategoryPurpose(PaymentTypeInformation.CATEGORY_PURPOSE)
                .withLocalInstrument(PaymentTypeInformation.LOCAL_INSTRUMENT)
                .withServiceLevel(PaymentTypeInformation.SERVICE_LEVEL)
                .build();
    }

    public PaymentTypeInformationEntity(Builder builder) {
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
