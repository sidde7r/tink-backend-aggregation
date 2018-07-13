package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MobileBankIdVerifyResponse extends AbstractResponse {
    private String easyLoginId;
    private String serverTime;
    private String status;
    private boolean extendedUsage;

    public String getEasyLoginId() {
        return easyLoginId;
    }

    public String getServerTime() {
        return serverTime;
    }

    public String getStatus() {
        return status;
    }

    public void setEasyLoginId(String easyLoginId) {
        this.easyLoginId = easyLoginId;
    }

    public void setServerTime(String serverTime) {
        this.serverTime = serverTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isExtendedUsage() {
        return extendedUsage;
    }

    public void setExtendedUsage(boolean extendedUsage) {
        this.extendedUsage = extendedUsage;
    }
}
