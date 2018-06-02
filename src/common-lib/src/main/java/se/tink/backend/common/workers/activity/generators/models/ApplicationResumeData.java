package se.tink.backend.common.workers.activity.generators.models;

public class ApplicationResumeData {
    private String applicationId;

    public ApplicationResumeData () {

    }

    public ApplicationResumeData(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }
}
