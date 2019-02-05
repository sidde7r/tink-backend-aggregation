package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.authenticator.entities.MessageEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.authenticator.entities.StatusEntity;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class InitiateBankIdResponse {
    private boolean success;
    private boolean restartLogon;
    private String appletTag;
    @JsonProperty("devicename")
    private String deviceName;
    private boolean retry;
    private StatusEntity status;
    private MessageEntity message;
    private String bidClientId;
    private boolean norId;
    private boolean userAlreadyAuthenticated;

    public boolean isSuccess() {
        return success;
    }

    public boolean isRestartLogon() {
        return restartLogon;
    }

    public String getAppletTag() {
        return appletTag;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public boolean isRetry() {
        return retry;
    }

    public StatusEntity getStatus() {
        return status;
    }

    public MessageEntity getMessage() {
        return message;
    }

    public String getBidClientId() {
        return bidClientId;
    }

    public boolean isNorId() {
        return norId;
    }

    public boolean isUserAlreadyAuthenticated() {
        return userAlreadyAuthenticated;
    }
}
