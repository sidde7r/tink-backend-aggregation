package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

@JsonObject
public class DebtorEntity {
    private String name;
    private PostalAddressEntity postalAddress;
    private IdEntity privateId;

    public static DebtorEntity of(PaymentRequest paymentRequest) {
        return new DebtorEntity.Builder()
                .withName(paymentRequest.getPayment().getCreditor().getName())
                .build();
    }

    public DebtorEntity(Builder builder) {
        this.name = builder.name;
        this.postalAddress = builder.postalAddress;
        this.privateId = builder.privateId;
    }

    public DebtorEntity() {}

    public static class Builder {
        private String name;
        private PostalAddressEntity postalAddress;
        private IdEntity privateId;

        public Builder withPostalAddress(PostalAddressEntity postalAddress) {
            this.postalAddress = postalAddress;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withPrivateId(IdEntity privateId) {
            this.privateId = privateId;
            return this;
        }

        public DebtorEntity build() {
            return new DebtorEntity(this);
        }
    }
}
