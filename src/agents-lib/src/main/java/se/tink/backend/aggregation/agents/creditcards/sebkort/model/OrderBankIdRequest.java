package se.tink.backend.aggregation.agents.creditcards.sebkort.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderBankIdRequest {
    private String subject;
    private boolean useAnotherDevice = true;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public boolean isUseAnotherDevice() {
        return useAnotherDevice;
    }

    public void setUseAnotherDevice(boolean useAnotherDevice) {
        this.useAnotherDevice = useAnotherDevice;
    }

}
