package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
public class AccountEntity {

    private String iban;

    public AccountEntity() {}

    public String getIban() {
        return iban;
    }

    @JsonIgnore
    public static AccountEntity creditorOf(PaymentRequest paymentRequest) {
        return new AccountEntity(paymentRequest.getPayment().getCreditor().getAccountNumber());
    }

    @JsonIgnore
    public static AccountEntity debtorOf(PaymentRequest paymentRequest) {
        return new AccountEntity(paymentRequest.getPayment().getDebtor().getAccountNumber());
    }

    @JsonIgnore
    public Creditor toTinkCreditor() {
        return new Creditor(new IbanIdentifier(iban));
    }

    @JsonIgnore
    public Debtor toTinkDebtor() {
        return new Debtor(new IbanIdentifier(iban));
    }

    public AccountEntity(String iban) {
        this.iban = iban;
    }
}
