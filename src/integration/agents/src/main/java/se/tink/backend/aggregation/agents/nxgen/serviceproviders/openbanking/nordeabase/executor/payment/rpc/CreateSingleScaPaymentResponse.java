package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.rpc.NordeaBaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class CreateSingleScaPaymentResponse extends NordeaBaseResponse {
    private SingleScaPaymentResponse response;
}
