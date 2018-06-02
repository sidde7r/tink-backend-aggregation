package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.investment.entities.ResponseStatusEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RecipientsResponse {
    @JsonProperty("Body")
    private RecipientsResponseBody body;

    @JsonProperty("ResponseStatus")
    private ResponseStatusEntity responseStatus;

    public RecipientsResponseBody getBody() {
        return body;
    }

    public ResponseStatusEntity getResponseStatus() {
        return responseStatus;
    }

    public void setBody(RecipientsResponseBody body) {
        this.body = body;
    }

    public void setResponseStatus(ResponseStatusEntity responseStatus) {
        this.responseStatus = responseStatus;
    }
}
