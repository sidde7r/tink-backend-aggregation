package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.payments.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import net.minidev.json.annotate.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class DebtorAccountEntity {
    private String iban;
    private String bban;

    public DebtorAccountEntity() {}

    @JsonIgnore
    private DebtorAccountEntity(Builder builder) {
        this.iban = builder.iban;
        this.bban = builder.bban;
    }

    public Debtor toTinkDebtor() {
        return iban != null
                ? new Debtor(new IbanIdentifier(iban))
                : new Debtor(
                        AccountIdentifier.create(
                                Type.DK, bban)); // should be DE, but there isn't one for DE
    }

    @JsonIgnore
    public static DebtorAccountEntity of(PaymentRequest paymentRequest) {
        String accountNumber = paymentRequest.getPayment().getDebtor().getAccountNumber();
        if (paymentRequest.getPayment().getDebtor().getAccountIdentifierType() == Type.IBAN) {
            return new DebtorAccountEntity.Builder().withIban(accountNumber).build();
        } else {
            return new DebtorAccountEntity.Builder().withBban(accountNumber).build();
        }
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

        public DebtorAccountEntity build() {
            return new DebtorAccountEntity(this);
        }
    }
}
