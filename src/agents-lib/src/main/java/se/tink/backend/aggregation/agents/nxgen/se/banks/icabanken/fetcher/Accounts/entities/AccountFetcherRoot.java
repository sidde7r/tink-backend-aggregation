package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.Accounts.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountFetcherRoot {
    @JsonProperty("Body")
    private AccountBodyEntity body;
    @JsonProperty("ResponseStatus")
    private ResponseStatusEntity responseStatus;

    public AccountBodyEntity getBody() {
        return body;
    }

    public ResponseStatusEntity getResponseStatus() {
        return responseStatus;
    }
}
