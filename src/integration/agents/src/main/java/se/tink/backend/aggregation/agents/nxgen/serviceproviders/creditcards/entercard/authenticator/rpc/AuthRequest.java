package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.authenticator.rpc;

import org.codehaus.jackson.annotate.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthRequest {

    @JsonProperty("SAMLResponse")
    private String responseSAML;

    @JsonProperty("TARGET")
    private String target;

    public AuthRequest(String responseSAML, String target) {
        this.responseSAML = responseSAML;
        this.target = target;
    }
}
