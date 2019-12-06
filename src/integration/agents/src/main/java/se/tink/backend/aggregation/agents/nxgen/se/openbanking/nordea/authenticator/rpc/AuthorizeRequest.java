package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizeRequest {
    @JsonProperty private long duration;

    @JsonProperty private String language;

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
            String language,
            String psuId,
            String redirectUri,
            String responseType,
            List<String> scope,
            String state) {
        this.duration = duration;
        this.language = language;
        this.redirectUri = redirectUri;
        this.psuId = psuId;
        this.responseType = responseType;
        this.scope = scope;
        this.state = state;
    }
}
