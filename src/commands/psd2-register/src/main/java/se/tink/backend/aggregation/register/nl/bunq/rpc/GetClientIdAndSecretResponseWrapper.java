package se.tink.backend.aggregation.register.nl.bunq.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.entities.PaginationEntity;

public class GetClientIdAndSecretResponseWrapper {
    @JsonProperty("Response")
    private List<GetClientIdAndSecretResponse> response;

    @JsonProperty("Pagination")
    private PaginationEntity pagination;

    public List<GetClientIdAndSecretResponse> getResponse() {
        return response;
    }
}
