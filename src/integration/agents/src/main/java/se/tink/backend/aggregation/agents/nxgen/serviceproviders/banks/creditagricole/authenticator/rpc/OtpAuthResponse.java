package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.rpc.DefaultResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OtpAuthResponse extends DefaultResponse {

    private String slToken;
    private String llToken;
    private String serverRefDate;

    public String getSlToken() {
        return slToken;
    }

    public void setSlToken(String slToken) {
        this.slToken = slToken;
    }

    public String getLlToken() {
        return llToken;
    }

    public void setLlToken(String llToken) {
        this.llToken = llToken;
    }

    public String getServerRefDate() {
        return serverRefDate;
    }

    public void setServerRefDate(String serverRefDate) {
        this.serverRefDate = serverRefDate;
    }
}
