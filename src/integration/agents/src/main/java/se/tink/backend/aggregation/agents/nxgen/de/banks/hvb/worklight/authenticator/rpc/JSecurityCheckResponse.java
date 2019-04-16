package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.rpc;

import org.codehaus.jackson.annotate.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class JSecurityCheckResponse {
    @JsonProperty private String username;
    @JsonProperty private String px2;
    @JsonProperty private String secP;

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setPx2(String px2) {
        this.px2 = px2;
    }

    public String getPx2() {
        return px2;
    }

    public void setSecP(String secP) {
        this.secP = secP;
    }

    public String getSecP() {
        return secP;
    }
}
