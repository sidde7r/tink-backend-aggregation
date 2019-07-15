package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
public class DebtorEntity {
    private String bban;
    private String currency;

    public DebtorEntity() {}

    private DebtorEntity(Builder builder) {
        this.bban = builder.iban;
        this.currency = builder.currency;
    }

    public static DebtorEntity of(PaymentRequest paymentRequest) {
        return new DebtorEntity.Builder()
                .withIban(paymentRequest.getPayment().getDebtor().getAccountNumber())
                .withCurrency(paymentRequest.getPayment().getCurrency())
                .build();
    }

    public Debtor toTinkDebtor() {
        return new Debtor(AccountIdentifier.create(Type.DK, bban));
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

        public DebtorEntity build() {
            return new DebtorEntity(this);
        }
    }
}
