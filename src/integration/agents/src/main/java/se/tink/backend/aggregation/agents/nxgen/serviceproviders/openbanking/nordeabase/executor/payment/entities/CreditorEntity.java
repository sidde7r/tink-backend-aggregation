package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.enums.NordeaAccountType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.payment.rpc.Creditor;

@JsonObject
public class CreditorEntity {
    private AccountEntity account;
    private String message;
    private String name;

    private ReferenceEntity reference;

    public CreditorEntity() {}

    public ReferenceEntity getReference() {
        return reference;
    }

    @JsonIgnore
    private CreditorEntity(Builder builder) {
        this.account = builder.account;
        this.message = builder.message;
        this.name = builder.name;
        this.reference = builder.reference;
    }

    @JsonIgnore
    public static CreditorEntity of(PaymentRequest paymentRequest) {
        return new CreditorEntity.Builder()
                .withAccount(
                        new AccountEntity(
                                NordeaAccountType.mapToNordeaAccountType(
                                                paymentRequest
                                                        .getPayment()
                                                        .getCreditor()
                                                        .getAccountIdentifierType())
                                        .name(),
                                paymentRequest.getPayment().getCurrency(),
                                paymentRequest.getPayment().getCreditor().getAccountNumber()))
                .build();
    }

    @JsonIgnore
    public Creditor toTinkCreditor() {
        return new Creditor(account.toTinkAccountIdentifier());
    }

    public static class Builder {
        private AccountEntity account;
        private String message;
        private String name;
        private ReferenceEntity reference;

        public Builder withAccount(AccountEntity account) {
            this.account = account;
            return this;
        }

        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withReference(ReferenceEntity reference) {
            this.reference = reference;
            return this;
        }

        public CreditorEntity build() {
            return new CreditorEntity(this);
        }
    }
}
