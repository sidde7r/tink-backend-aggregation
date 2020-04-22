package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.enums.NordeaAccountType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.account.AccountIdentifier;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setReference(ReferenceEntity reference) {
        this.reference = reference;
    }

    @JsonIgnore
    private CreditorEntity(Builder builder) {
        this.account = builder.account;
        this.name = builder.name;
    }

    @JsonIgnore
    public static CreditorEntity of(PaymentRequest paymentRequest) {
        CreditorEntity creditorEntity =
                new CreditorEntity.Builder().withAccount(getAccount(paymentRequest)).build();
        String referenceMessage = paymentRequest.getPayment().getReference().getValue();
        if (isTypeBgPg(paymentRequest.getPayment().getCreditor().getAccountIdentifierType())) {
            creditorEntity.setReference(new ReferenceEntity("OCR", referenceMessage));
        } else {
            creditorEntity.setMessage(referenceMessage);
        }
        return creditorEntity;
    }

    @JsonIgnore
    public Creditor toTinkCreditor() {
        return new Creditor(account.toTinkAccountIdentifier());
    }

    public static class Builder {
        private AccountEntity account;
        private String name;

        public Builder withAccount(AccountEntity account) {
            this.account = account;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public CreditorEntity build() {
            return new CreditorEntity(this);
        }
    }

    @JsonIgnore
    private static AccountEntity getAccount(PaymentRequest paymentRequest) {
        return new AccountEntity(
                NordeaAccountType.mapToNordeaAccountType(
                                paymentRequest
                                        .getPayment()
                                        .getCreditor()
                                        .getAccountIdentifierType())
                        .name(),
                paymentRequest.getPayment().getCurrency(),
                paymentRequest.getPayment().getCreditor().getAccountNumber());
    }

    @JsonIgnore
    private static boolean isTypeBgPg(AccountIdentifier.Type accountIdentifierType) {
        switch (accountIdentifierType) {
            case SE_BG:
            case SE_PG:
                return true;
        }
        return false;
    }
}
