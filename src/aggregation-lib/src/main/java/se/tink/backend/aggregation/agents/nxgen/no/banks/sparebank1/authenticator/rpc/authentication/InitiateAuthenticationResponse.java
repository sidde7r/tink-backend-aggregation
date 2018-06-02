package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.authentication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InitiateAuthenticationResponse {
    private String publicB;
    private String salt;
    @JsonProperty("_links")
    private HashMap<String, LinkEntity> links;

    public String getPublicB() {
        return publicB;
    }

    public void setPublicB(String publicB) {
        this.publicB = publicB;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public HashMap<String, LinkEntity> getLinks() {
        return links;
    }

    public void setLinks(
            HashMap<String, LinkEntity> links) {
        this.links = links;
    }
}
