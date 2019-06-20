package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

@JsonObject
public class AccountEntity {
    private String iban;
    private String bban;

    public static AccountEntity creditorOf(PaymentRequest paymentRequest) {
        String bban = paymentRequest.getPayment().getCreditor().getAccountNumber().substring(4);
        return new AccountEntity(
                paymentRequest.getPayment().getCreditor().getAccountNumber(), bban);
    }

    public static AccountEntity debtorOf(PaymentRequest paymentRequest) {
        String bban = paymentRequest.getPayment().getDebtor().getAccountNumber().substring(4);
        return new AccountEntity(paymentRequest.getPayment().getDebtor().getAccountNumber(), bban);
    }

    public AccountEntity(String iban, String bban) {
        this.iban = iban;
        this.bban = bban;
    }

    public AccountEntity() {}

    public String getIban() {
        return iban;
    }
}
