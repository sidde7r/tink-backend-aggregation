package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

@JsonObject
public class AccountEntity {
    private String iban;
    private String bban;

    @JsonIgnore
    public static AccountEntity creditorOf(PaymentRequest paymentRequest) {
        String bban = paymentRequest.getPayment().getCreditor().getAccountNumber().substring(4);
        return new AccountEntity(
                paymentRequest.getPayment().getCreditor().getAccountNumber(), bban);
    }

    @JsonIgnore
    public static AccountEntity debtorOf(PaymentRequest paymentRequest) {
        String bban = paymentRequest.getPayment().getDebtor().getAccountNumber().substring(4);
        return new AccountEntity(paymentRequest.getPayment().getDebtor().getAccountNumber(), bban);
    }

    @JsonCreator
    public AccountEntity(@JsonProperty("iban") String iban, @JsonProperty("bban") String bban) {
        this.iban = iban;
        this.bban = bban;
    }

    public String getIban() {
        return iban;
    }
}
