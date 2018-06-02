package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.authentication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RestRootResponse {
    @JsonProperty("_links")
    private HashMap<String, LinkEntity> links;
    @JsonProperty("_authorizationLevel")
    private Integer authorizationLevel;

    public HashMap<String, LinkEntity> getLinks() {
        return links;
    }

    public Integer getAuthorizationLevel() {
        return authorizationLevel;
    }
}
