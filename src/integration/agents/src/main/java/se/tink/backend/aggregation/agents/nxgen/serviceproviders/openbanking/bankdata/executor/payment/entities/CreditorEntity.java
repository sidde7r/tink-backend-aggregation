package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.payment.rpc.Creditor;

@JsonObject
public class CreditorEntity {
    private String bban;
    private String currency;

    public CreditorEntity() {}

    private CreditorEntity(Builder builder) {
        this.bban = builder.iban;
        this.currency = builder.currency;
    }

    public static CreditorEntity of(PaymentRequest paymentRequest) {
        return new CreditorEntity.Builder()
                .withIban(paymentRequest.getPayment().getCreditor().getAccountNumber())
                .withCurrency(paymentRequest.getPayment().getCurrency())
                .build();
    }

    public Creditor toTinkCreditor() {
        return new Creditor(AccountIdentifier.create(Type.DK, bban));
    }

    public String getBban() {
        return bban;
    }

    public String getCurrency() {
        return currency;
    }

    public static class Builder {
        private String iban;
        private String currency;

        public Builder withIban(String iban) {
            this.iban = iban;
            return this;
        }

        public Builder withCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        public CreditorEntity build() {
            return new CreditorEntity(this);
        }
    }
}
