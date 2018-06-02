package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.authenticator.entities.MessageEntity;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CollectChallengeResponse {
    private boolean success;
    private boolean restartLogon;
    private String devicename;
    private boolean retry;
    private MessageEntity message;
    private boolean norId;
    private boolean userAlreadyAuthenticated;

    public boolean isSuccess() {
        return success;
    }

    public boolean isRestartLogon() {
        return restartLogon;
    }

    public String getDevicename() {
        return devicename;
    }

    public boolean isRetry() {
        return retry;
    }

    public MessageEntity getMessage() {
        return message;
    }

    public boolean isNorId() {
        return norId;
    }

    public boolean isUserAlreadyAuthenticated() {
        return userAlreadyAuthenticated;
    }
}
