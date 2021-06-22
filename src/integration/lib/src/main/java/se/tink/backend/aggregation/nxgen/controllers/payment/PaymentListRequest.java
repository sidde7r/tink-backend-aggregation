package se.tink.backend.aggregation.nxgen.controllers.payment;

import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PaymentListRequest {

    private final List<PaymentRequest> paymentRequestList;

    public PaymentListRequest(PaymentRequest paymentRequest) {
        this.paymentRequestList = ImmutableList.of(paymentRequest);
    }
}
