package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.entities.GroupHeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.entities.PaymentListResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetPaymentsResponse {
    @JsonProperty("group_header")
    private GroupHeaderEntity groupHeader;

    private PaymentListResponseEntity response;

    public PaymentListResponseEntity getResponse() {
        return response;
    }
}
