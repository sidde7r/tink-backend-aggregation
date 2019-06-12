package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
public class AccountEntity {
    private String description;
    private String iban;
    private String bban;
    private String pan;
    private String maskedPan;
    private String msisdn;
    private String currency;

    public AccountEntity() {}

    public String getIban() {
        return iban;
    }

    public static AccountEntity creditorOf(PaymentRequest paymentRequest) {
        String bban = paymentRequest.getPayment().getCreditor().getAccountNumber().substring(4);
        return new Builder()
                .withIban(paymentRequest.getPayment().getCreditor().getAccountNumber())
                .withBban(bban)
                .withCurrency(paymentRequest.getPayment().getCurrency())
                .build();
    }

    public static AccountEntity debtorOf(PaymentRequest paymentRequest) {
        String bban = paymentRequest.getPayment().getDebtor().getAccountNumber().substring(4);
        return new Builder()
                .withIban(paymentRequest.getPayment().getDebtor().getAccountNumber())
                .withBban(bban)
                .withCurrency(paymentRequest.getPayment().getCurrency())
                .build();
    }

    @JsonIgnore
    public Creditor toTinkCreditor() {
        return new Creditor(new IbanIdentifier(iban));
    }

    @JsonIgnore
    public Debtor toTinkDebtor() {
        return new Debtor(new IbanIdentifier(iban));
    }

    private AccountEntity(Builder builder) {
        this.description = builder.description;
        this.iban = builder.iban;
        this.bban = builder.bban;
        this.pan = builder.pan;
        this.maskedPan = builder.maskedPan;
        this.msisdn = builder.msisdn;
        this.currency = builder.currency;
    }

    public static class Builder {
        private String description;
        private String iban;
        private String bban;
        private String pan;
        private String maskedPan;
        private String msisdn;
        private String currency;

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withIban(String iban) {
            this.iban = iban;
            return this;
        }

        public Builder withBban(String bban) {
            this.bban = bban;
            return this;
        }

        public Builder withPan(String pan) {
            this.pan = pan;
            return this;
        }

        public Builder withMaskedPan(String maskedPan) {
            this.maskedPan = maskedPan;
            return this;
        }

        public Builder withMsisdn(String msisdn) {
            this.msisdn = msisdn;
            return this;
        }

        public Builder withCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        public AccountEntity build() {
            return new AccountEntity(this);
        }
    }
}
