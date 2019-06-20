package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

@JsonObject
public class InstructedAmountEntity {
    private String currency;
    private double amount;

    public static InstructedAmountEntity of(PaymentRequest paymentRequest) {
        return new InstructedAmountEntity(
                paymentRequest.getPayment().getAmount().getCurrency(),
                paymentRequest.getPayment().getAmount().getValue());
    }

    public String getCurrency() {
        return currency;
    }

    public double getAmount() {
        return amount;
    }

    public InstructedAmountEntity() {}

    public InstructedAmountEntity(String currency, double amount) {
        this.currency = currency;
        this.amount = amount;
    }
}
