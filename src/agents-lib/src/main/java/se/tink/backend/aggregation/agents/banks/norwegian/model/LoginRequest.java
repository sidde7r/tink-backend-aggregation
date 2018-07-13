package se.tink.backend.aggregation.agents.banks.norwegian.model;

public class LoginRequest {
    private String subject;
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
}
