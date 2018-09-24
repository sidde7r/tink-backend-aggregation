package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.LclConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BpiMetaData {
    private String sessionId;
    @JsonProperty("mobile_data")
    private String mobileData;
    private String channel;

    private BpiMetaData(String sessionId) {
        this.sessionId = sessionId;
        this.mobileData = "";
        this.channel = LclConstants.Authentication.CHANNEL;
    }

    public static BpiMetaData create(String sessionId) {
        return new BpiMetaData(sessionId);
    }
}
