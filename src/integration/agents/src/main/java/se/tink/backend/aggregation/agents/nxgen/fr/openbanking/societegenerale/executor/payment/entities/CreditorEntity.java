package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities;

import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.GenericIdentificationEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.PostalAddressEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

@JsonObject
public class CreditorEntity {
    private String name;
    private PostalAddressEntity postalAddress;
    private GenericIdentificationEntity organisationId;

    public static CreditorEntity of(PaymentRequest paymentRequest) {
        return new CreditorEntity.Builder()
                .withName(paymentRequest.getPayment().getCreditor().getName())
                .build();
    }

    private CreditorEntity(Builder builder) {
        this.name = builder.name;
        this.postalAddress = builder.postalAddress;
        this.organisationId = builder.organisationId;
    }

    public CreditorEntity() {}

    public String getName() {
        return name;
    }

    public static class Builder {
        private String name;
        private PostalAddressEntity postalAddress;
        private GenericIdentificationEntity organisationId;

        public Builder withPostalAddress(PostalAddressEntity postalAddress) {
            this.postalAddress = postalAddress;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withReference(GenericIdentificationEntity organisationId) {
            this.organisationId = organisationId;
            return this;
        }

        public CreditorEntity build() {
            return new CreditorEntity(this);
        }
    }
}
