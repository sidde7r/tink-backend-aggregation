package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

@JsonObject
public class DebtorEntity {
    private String accountNumber;

    public DebtorEntity() {}

    public DebtorEntity(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    @JsonIgnore
    public static DebtorEntity of(PaymentRequest paymentRequest) {
        return new DebtorEntity(paymentRequest.getPayment().getDebtor().getAccountNumber());
    }
}
