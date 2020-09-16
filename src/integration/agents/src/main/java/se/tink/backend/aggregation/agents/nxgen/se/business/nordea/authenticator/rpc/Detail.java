package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class Detail {

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("param")
    private String param;

    @JsonProperty("more_info")
    private String moreInfo;

    public String getReason() {
        return reason;
    }

    public String getParam() {
        return param;
    }

    public String getMoreInfo() {
        return moreInfo;
    }
}
