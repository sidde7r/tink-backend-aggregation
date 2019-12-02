package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.DanskebankV2Constants.ErrorCode;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.DanskebankV2Constants.ErrorMessage;

public class AbstractResponse {
    @JsonProperty("MagicKey")
    protected String magicKey;

    @JsonProperty("ServerTime")
    protected String serverTime;

    @JsonProperty("Status")
    protected StatusEntity status;

    public String getMagicKey() {
        return magicKey;
    }

    public void setMagicKey(String magicKey) {
        this.magicKey = magicKey;
    }

    public String getServerTime() {
        return serverTime;
    }

    public void setServerTime(String serverTime) {
        this.serverTime = serverTime;
    }

    public StatusEntity getStatus() {
        return status;
    }

    public void setStatus(StatusEntity status) {
        this.status = status;
    }

    @JsonIgnore
    public boolean isStatusOk() {
        return status.getStatusCode() == 0 && Strings.isNullOrEmpty(status.getStatusText());
    }

    @JsonIgnore
    public boolean isStatusAuthorizationNotPossible() {
        return ErrorCode.AUTHORIZATION_NOT_POSSIBLE == getStatus().getStatusCode()
                && ErrorMessage.AUTHORIZATION_NOT_POSSIBLE.equalsIgnoreCase(
                        getStatus().getStatusText());
    }

    @JsonIgnore
    public boolean isStatusTechnicalError() {
        return ErrorCode.TECHNICAL_ERROR == getStatus().getStatusCode()
                && getStatus().getStatusText().toLowerCase().contains(ErrorMessage.TECHNICAL_ERROR);
    }

    @JsonIgnore
    public boolean isUnauthorizedError() {
        return ErrorCode.UNAUTHORIZED == getStatus().getStatusCode()
                && (getStatus().getStatusText().toLowerCase().contains(ErrorMessage.UNAUTHORIZED));
    }

    @JsonIgnore
    public boolean isSessionExpiredError() {
        return ErrorCode.UNAUTHORIZED == getStatus().getStatusCode()
                && (getStatus()
                        .getStatusText()
                        .toLowerCase()
                        .contains(ErrorMessage.SESSION_EXPIRED));
    }
}
