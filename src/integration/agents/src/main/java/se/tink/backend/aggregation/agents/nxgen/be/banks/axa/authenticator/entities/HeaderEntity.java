package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class HeaderEntity {

    private String type;
    private String uid;
    private String deviceId;
    private String sessionId;

    private HeaderEntity() {}

    public HeaderEntity(String uid) {
        this.type = "uid";
        this.uid = uid;
    }

    public String getType() {
        return type;
    }

    public String getUid() {
        return uid;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getSessionId() {
        return sessionId;
    }
}
