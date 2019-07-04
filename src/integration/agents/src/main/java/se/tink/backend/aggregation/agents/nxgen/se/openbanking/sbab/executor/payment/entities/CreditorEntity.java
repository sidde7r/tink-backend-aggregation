package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

@JsonObject
public class CreditorEntity {
    private String accountNumber;

    public CreditorEntity(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public CreditorEntity() {}

    @JsonIgnore
    public static CreditorEntity of(PaymentRequest paymentRequest) {
        return new CreditorEntity(paymentRequest.getPayment().getCreditor().getAccountNumber());
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}
