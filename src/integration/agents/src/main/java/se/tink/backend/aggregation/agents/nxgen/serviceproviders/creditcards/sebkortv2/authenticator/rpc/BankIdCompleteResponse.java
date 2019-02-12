package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankIdCompleteResponse {
    @JsonProperty("SAMLResponse")
    private String responseSAML;

    private String target;

    public String getResponseSAML() {
        return responseSAML;
    }
}
