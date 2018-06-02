package se.tink.backend.system.rpc;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import java.util.UUID;
import se.tink.backend.core.application.ApplicationState;

public class UpdateApplicationRequest {
    private UUID userId;
    private UUID credentialsId; //TODO: this can be removed later on
    private UUID applicationId;
    private ApplicationState applicationState;

    public UpdateApplicationRequest() {
    }

    public UpdateApplicationRequest(UUID userId, UUID applicationId,
            ApplicationState applicationState) {
        this.userId = userId;
        this.applicationId = applicationId;
        this.applicationState = applicationState;
    }

    public UpdateApplicationRequest(UUID userId, UUID credentialsId, UUID applicationId,
            ApplicationState applicationState) {
        this.userId = userId;
        this.credentialsId = credentialsId;
        this.applicationId = applicationId;
        this.applicationState = applicationState;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UpdateApplicationRequest that = (UpdateApplicationRequest) o;

        return Objects.equal(this.userId, that.userId) &&
                Objects.equal(this.credentialsId, that.credentialsId) &&
                Objects.equal(this.applicationId, that.applicationId) &&
                Objects.equal(this.applicationState, that.applicationState);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId, credentialsId, applicationId, applicationState);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("userId", userId)
                .add("credentialsId", credentialsId)
                .add("applicationId", applicationId)
                .add("applicationState", applicationState)
                .toString();
    }
}
