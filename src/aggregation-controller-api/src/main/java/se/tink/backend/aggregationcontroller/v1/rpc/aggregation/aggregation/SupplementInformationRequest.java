package se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation;

public class SupplementInformationRequest {
    public static final String QUEUE_NAME = "supplementInformation";

    private String credentialsId;
    private String supplementalInformation;

    public String getCredentialsId() {
        return credentialsId;
    }

    public String getSupplementalInformation() {
        return supplementalInformation;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public void setSupplementalInformation(String supplementalInformation) {
        this.supplementalInformation = supplementalInformation;
    }
}
