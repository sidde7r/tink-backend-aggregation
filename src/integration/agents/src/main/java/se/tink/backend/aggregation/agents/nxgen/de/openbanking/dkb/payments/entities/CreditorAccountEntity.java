package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.payments.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import net.minidev.json.annotate.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.payment.rpc.Creditor;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class CreditorAccountEntity {
    private String iban;
    private String bban;

    public CreditorAccountEntity() {}

    @JsonIgnore
    private CreditorAccountEntity(Builder builder) {
        this.iban = builder.iban;
        this.bban = builder.bban;
    }

    @JsonIgnore
    public static CreditorAccountEntity of(PaymentRequest paymentRequest) {
        String accountNumber = paymentRequest.getPayment().getCreditor().getAccountNumber();
        if (paymentRequest.getPayment().getCreditor().getAccountIdentifierType() == Type.IBAN) {
            return new CreditorAccountEntity.Builder().withIban(accountNumber).build();
        } else {
            return new CreditorAccountEntity.Builder().withBban(accountNumber).build();
        }
    }

    public Creditor toTinkCreditor() {
        return iban != null
                ? new Creditor(new IbanIdentifier(iban))
                : new Creditor(
                        AccountIdentifier.create(
                                Type.DK, bban)); // should be DE but there isn't one for DE
    }

    public static class Builder {
        private String iban;
        private String bban;

        public Builder withIban(String iban) {
            this.iban = iban;
            return this;
        }

        public Builder withBban(String bban) {
            this.bban = bban;
            return this;
        }

        public CreditorAccountEntity build() {
            return new CreditorAccountEntity(this);
        }
    }
}
