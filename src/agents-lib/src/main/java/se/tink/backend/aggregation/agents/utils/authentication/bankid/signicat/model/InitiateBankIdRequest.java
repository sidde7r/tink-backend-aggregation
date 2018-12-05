package se.tink.backend.aggregation.agents.utils.authentication.bankid.signicat.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InitiateBankIdRequest {
    private String subject;
    private String apiKey;
    private boolean useAnotherDevice = true;

    public boolean isUseAnotherDevice() {
        return useAnotherDevice;
    }

    public void setUseAnotherDevice(boolean useAnotherDevice) {
        this.useAnotherDevice = useAnotherDevice;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
