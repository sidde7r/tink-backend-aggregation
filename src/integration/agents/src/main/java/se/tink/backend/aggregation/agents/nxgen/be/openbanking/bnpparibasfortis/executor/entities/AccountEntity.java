package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
public class AccountEntity {
    private String iban;

    private AccountEntity(String iban) {
        this.iban = iban;
    }

    public AccountEntity() {}

    public static AccountEntity creditorOf(PaymentRequest paymentRequest) {
        return new AccountEntity(paymentRequest.getPayment().getCreditor().getAccountNumber());
    }

    public static AccountEntity debtorOf(PaymentRequest paymentRequest) {
        return new AccountEntity(paymentRequest.getPayment().getDebtor().getAccountNumber());
    }

    public Creditor toTinkCreditor() {
        return new Creditor(new IbanIdentifier(iban));
    }

    public Debtor toTinkDebtor() {
        return new Debtor(new IbanIdentifier(iban));
    }
}
