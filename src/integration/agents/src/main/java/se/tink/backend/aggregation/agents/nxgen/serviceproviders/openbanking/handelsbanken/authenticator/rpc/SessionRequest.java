package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class SessionRequest {

    @JsonProperty("psu_id")
    private String personalId;

    @JsonProperty("psu_client_ip")
    private String psuClientIp;

    @JsonProperty("psu_id_type")
    private String personalIdTp;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("client_id")
    private String clientId;

    public SessionRequest(
            String clientId,
            String scope,
            String psuClientIp,
            String personalId,
            String personalIdTp) {
        this.personalId = personalId;
        this.psuClientIp = psuClientIp;
        this.personalIdTp = personalIdTp;
        this.scope = scope;
        this.clientId = clientId;
    }

    public String getPersonalId() {
        return personalId;
    }

    public String getPsuClientIp() {
        return psuClientIp;
    }

    public String getPersonalIdTp() {
        return personalIdTp;
    }

    public String getScope() {
        return scope;
    }

    public String getClientId() {
        return clientId;
    }
}
