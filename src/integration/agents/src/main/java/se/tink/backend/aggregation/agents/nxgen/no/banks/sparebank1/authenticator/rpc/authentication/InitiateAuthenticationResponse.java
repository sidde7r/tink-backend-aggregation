package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitiateAuthenticationResponse {
    private String publicB;
    private String salt;

    @JsonProperty("_links")
    private HashMap<String, LinkEntity> links;

    public String getPublicB() {
        return publicB;
    }

    public String getSalt() {
        return salt;
    }

    public HashMap<String, LinkEntity> getLinks() {
        return links;
    }
}
