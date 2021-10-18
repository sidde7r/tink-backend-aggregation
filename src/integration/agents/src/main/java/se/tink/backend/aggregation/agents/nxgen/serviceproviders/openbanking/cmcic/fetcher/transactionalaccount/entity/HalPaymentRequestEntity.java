package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.enums.PaymentStatus;

@JsonObject
public class HalPaymentRequestEntity {
    @JsonProperty("paymentRequest")
    private PaymentResponseEntity paymentRequest = null;

    public PaymentResponseEntity getPaymentRequest() {
        return paymentRequest;
    }

    public void setPaymentRequest(PaymentResponseEntity paymentRequest) {
        this.paymentRequest = paymentRequest;
    }

    public boolean isPending() {
        return Optional.ofNullable(paymentRequest)
                .map(PaymentResponseEntity::getPaymentInformationStatusCode)
                .map(PaymentInformationStatusCodeEntity::getPaymentStatus)
                .filter(paymentStatus -> paymentStatus == PaymentStatus.PENDING)
                .isPresent();
    }
}
