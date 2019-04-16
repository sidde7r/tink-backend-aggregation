package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.entities.PaymentListResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.rpc.NordeaBaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetPaymentsResponse extends NordeaBaseResponse {

    private PaymentListResponseEntity response;

    public PaymentListResponseEntity getResponse() {
        return response;
    }
}
