package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqResponse;

public class CreateDraftPaymentResponseWrapper {
    @JsonProperty("Response")
    private BunqResponse<CreateDraftPaymentResponse> response;

    public BunqResponse<CreateDraftPaymentResponse> getResponse() {
        return response;
    }
}
