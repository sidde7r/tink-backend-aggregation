package se.tink.backend.sms.gateways.rpc;

public class SmsResponse {
    private boolean success;
    private String payload;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
