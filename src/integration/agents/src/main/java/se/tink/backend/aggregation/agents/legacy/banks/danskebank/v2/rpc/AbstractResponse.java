package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;

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
}
