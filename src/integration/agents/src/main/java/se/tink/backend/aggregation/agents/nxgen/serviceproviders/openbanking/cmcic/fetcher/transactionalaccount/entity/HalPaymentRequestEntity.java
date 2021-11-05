package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.enums.PaymentStatus;

@Getter
@Setter
@JsonObject
public class HalPaymentRequestEntity {
    @JsonProperty("paymentRequest")
    private PaymentResponseEntity paymentRequest = null;

    public boolean isPending() {
        return Optional.ofNullable(paymentRequest)
                .map(PaymentResponseEntity::getPaymentInformationStatus)
                .map(PaymentInformationStatusEntity::getPaymentStatus)
                .filter(paymentStatus -> paymentStatus == PaymentStatus.PENDING)
                .isPresent();
    }
}
