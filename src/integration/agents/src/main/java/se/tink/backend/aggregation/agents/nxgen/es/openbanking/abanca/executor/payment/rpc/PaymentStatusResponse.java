package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.executor.payment.entity.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.executor.payment.entity.PaymentStatusEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class PaymentStatusResponse {

    @JsonProperty("data")
    private PaymentStatusEntity paymentStatus;

    private LinksEntity links;
}
