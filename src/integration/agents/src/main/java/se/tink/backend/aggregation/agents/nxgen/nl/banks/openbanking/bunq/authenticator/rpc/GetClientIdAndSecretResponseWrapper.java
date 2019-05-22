package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.BunqResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.entities.PaginationEntity;

public class GetClientIdAndSecretResponseWrapper {
    @JsonProperty("Response")
    private BunqResponse<GetClientIdAndSecretResponse> response;

    @JsonProperty("Pagination")
    private PaginationEntity pagination;

    public BunqResponse<GetClientIdAndSecretResponse> getResponse() {
        return response;
    }
}
