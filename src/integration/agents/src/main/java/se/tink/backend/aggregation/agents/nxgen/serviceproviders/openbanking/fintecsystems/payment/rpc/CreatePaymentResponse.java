package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Payment;

@NoArgsConstructor
@Getter
@Setter
@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CreatePaymentResponse {
    String wizardSessionKey;
    String transaction;

    @JsonIgnore
    public PaymentResponse toTinkPayment(CreatePaymentResponse createPaymentResponse) {
        Payment.Builder tinkPayment = new Payment.Builder();
        tinkPayment.withUniqueId(
                createPaymentResponse.getTransaction()); // FTS Unique transaction Id

        return new PaymentResponse(tinkPayment.build());
    }
}
