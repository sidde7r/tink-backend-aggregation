package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class AuthorizeRequest {
    @JsonProperty private long duration;

    @JsonProperty("psu_id")
    private String psuId;

    @JsonProperty("redirect_uri")
    private String redirectUri;

    @JsonProperty("response_type")
    private String responseType;

    @JsonProperty private List<String> scope;

    @JsonProperty private String state;

    public AuthorizeRequest(
            long duration,
            String psuId,
            String redirectUri,
            String responseType,
            List<String> scope,
            String state) {
        this.duration = duration;
        this.psuId = psuId;
        this.redirectUri = redirectUri;
        this.responseType = responseType;
        this.scope = scope;
        this.state = state;
    }
}
