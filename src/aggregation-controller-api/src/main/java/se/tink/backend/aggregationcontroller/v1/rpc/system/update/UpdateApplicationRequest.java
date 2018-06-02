package se.tink.backend.aggregationcontroller.v1.rpc.system.update;

import java.util.UUID;
import se.tink.backend.core.application.ApplicationState;

public class UpdateApplicationRequest {
    private UUID userId;
    private UUID credentialsId;
    private UUID applicationId;
    private ApplicationState applicationState;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getCredentialsId() {
        return credentialsId;
    }

    public void setCredentialsId(UUID credentialsId) {
        this.credentialsId = credentialsId;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    public ApplicationState getApplicationState() {
        return applicationState;
    }

    public void setApplicationState(ApplicationState applicationState) {
        this.applicationState = applicationState;
    }
}
