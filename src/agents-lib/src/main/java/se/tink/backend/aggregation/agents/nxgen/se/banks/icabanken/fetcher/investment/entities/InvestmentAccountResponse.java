package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.investment.entities;


import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InvestmentAccountResponse {
    @JsonProperty("Body")
    private BodyEntity body;
    @JsonProperty("ResponseStatus")
    private ResponseStatusEntity responseStatus;

    public BodyEntity getBody() {
        return body;
    }

    public ResponseStatusEntity getResponseStatus() {
        return responseStatus;
    }
}
