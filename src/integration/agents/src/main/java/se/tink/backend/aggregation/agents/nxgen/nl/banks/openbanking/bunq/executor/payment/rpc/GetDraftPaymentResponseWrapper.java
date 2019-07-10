package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqResponse;

public class GetDraftPaymentResponseWrapper {
    @JsonProperty("Response")
    private BunqResponse<GetDraftPaymentResponse> response;

    public BunqResponse<GetDraftPaymentResponse> getResponse() {
        return response;
    }
}
