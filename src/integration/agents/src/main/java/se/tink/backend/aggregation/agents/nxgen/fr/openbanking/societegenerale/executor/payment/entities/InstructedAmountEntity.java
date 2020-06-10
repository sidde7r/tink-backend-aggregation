package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class InstructedAmountEntity {
    private String currency;
    private double amount;

    public static InstructedAmountEntity of(PaymentRequest paymentRequest) {
        return InstructedAmountEntity.builder()
                .amount(paymentRequest.getPayment().getAmount().getValue())
                .currency(paymentRequest.getPayment().getAmount().getCurrency())
                .build();
    }
}
