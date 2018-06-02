package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.authenticator.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.rpc.SpankkiResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AddDeviceResponse extends SpankkiResponse {
    private String loginToken;
    private String deviceId;
    private Boolean isPAD;
    private List<String> serviceCodes;
    private String techBoaId;

    public String getLoginToken() {
        return loginToken;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public Boolean getPAD() {
        return isPAD;
    }

    public List<String> getServiceCodes() {
        return serviceCodes;
    }

    public String getTechBoaId() {
        return techBoaId;
    }
}
