package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanOverviewResponse {
    @JsonProperty("Body")
    private BodyEntity body;
    @JsonProperty("ResponseStatus")
    private ResponseStatusEntity responseStatus;

    public BodyEntity getBody() {
        Preconditions.checkState(body != null, "Expected a body object but it was null");
        return body;
    }

    public ResponseStatusEntity getResponseStatus() {
        return responseStatus;
    }
}
