package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.entities.PaymentListResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.rpc.NordeaBaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentType;

import java.util.List;
import java.util.stream.Collectors;

@JsonObject
public class GetPaymentsResponse extends NordeaBaseResponse {

    private PaymentListResponseEntity response;

    public PaymentListResponse toTinkPaymentListResponse(PaymentType paymentType) {
        List<PaymentResponse> paymentResponses =
                response.getPayments().stream()
                        .map(
                                paymentResponseEntity ->
                                        paymentResponseEntity.toTinkPaymentResponse(paymentType))
                        .collect(Collectors.toList());

        return new PaymentListResponse(paymentResponses);
    }
}
