package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.enums.NordeaPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.rpc.NordeaBaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Payment;

@Getter
@JsonObject
public class CancelPaymentResponse extends NordeaBaseResponse {

    private List<String> response;

    public PaymentResponse toTinkCancellablePaymentResponseWithStatus(
            NordeaPaymentStatus status, Payment payment) {
        return new PaymentResponse(
                new Payment.Builder()
                        .withUniqueId(payment.getUniqueId())
                        .withType(payment.getType())
                        .withStatus(NordeaPaymentStatus.mapToTinkPaymentStatus(status))
                        .build());
    }
}
