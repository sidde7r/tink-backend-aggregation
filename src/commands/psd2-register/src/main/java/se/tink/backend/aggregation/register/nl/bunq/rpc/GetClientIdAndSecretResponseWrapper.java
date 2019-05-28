package se.tink.backend.aggregation.register.nl.bunq.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.entities.PaginationEntity;

public class GetClientIdAndSecretResponseWrapper {
    @JsonProperty("Response")
    private BunqResponse<GetClientIdAndSecretResponse> response;

    @JsonProperty("Pagination")
    private PaginationEntity pagination;

    public BunqResponse<GetClientIdAndSecretResponse> getResponse() {
        return response;
    }
}
