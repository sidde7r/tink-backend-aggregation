package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonalCodeResponse extends AbstractResponse {
    private String easyLoginId;
    private boolean personalCodeChangeRequired;
    private String serverTime;

    public String getEasyLoginId() {
        return easyLoginId;
    }

    public String getServerTime() {
        return serverTime;
    }

    public boolean isPersonalCodeChangeRequired() {
        return personalCodeChangeRequired;
    }

    public void setEasyLoginId(String easyLoginId) {
        this.easyLoginId = easyLoginId;
    }

    public void setPersonalCodeChangeRequired(boolean personalCodeChangeRequired) {
        this.personalCodeChangeRequired = personalCodeChangeRequired;
    }

    public void setServerTime(String serverTime) {
        this.serverTime = serverTime;
    }
}
