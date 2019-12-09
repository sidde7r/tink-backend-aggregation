package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.entites;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

@JsonObject
public class AccountEntity {
    private String iban;

    public AccountEntity(String iban) {
        this.iban = iban;
    }

    public static AccountEntity debtorOf(PaymentRequest paymentRequest) {
        return new AccountEntity(paymentRequest.getPayment().getDebtor().getAccountNumber());
    }

    public static AccountEntity creditorOf(PaymentRequest paymentRequest) {
        return new AccountEntity(paymentRequest.getPayment().getCreditor().getAccountNumber());
    }
}
