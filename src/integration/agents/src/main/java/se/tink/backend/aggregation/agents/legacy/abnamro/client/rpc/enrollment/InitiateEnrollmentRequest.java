package se.tink.backend.aggregation.agents.abnamro.client.rpc.enrollment;

public class InitiateEnrollmentRequest {
    private String phoneNumber;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
