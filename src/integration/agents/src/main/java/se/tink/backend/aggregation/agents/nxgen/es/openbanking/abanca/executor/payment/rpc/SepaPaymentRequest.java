package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.executor.payment.entity.PaymentDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@EqualsAndHashCode
@AllArgsConstructor
@JsonObject
public class SepaPaymentRequest {

    @JsonProperty("data")
    private PaymentDataEntity paymentData;
}
