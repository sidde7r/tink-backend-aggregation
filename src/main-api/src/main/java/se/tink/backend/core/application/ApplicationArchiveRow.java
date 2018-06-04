package se.tink.backend.core.application;

import java.util.Date;
import java.util.UUID;
import se.tink.libraries.application.ApplicationType;

public class ApplicationArchiveRow {
    private UUID userId;
    private UUID applicationId;
    private String applicationType;
    private String status;
    private String content;
    private String externalId;
    private String notes;
    private Date timestamp;

    public ApplicationArchiveRow() {

    }

    public ApplicationArchiveRow(UUID userId, UUID applicationId, ApplicationType applicationType, Status status,
            String content) {
        this.userId = userId;
        this.applicationId = applicationId;
        this.applicationType = applicationType.name();
        this.status = status.name();
        this.content = content;
        this.timestamp = new Date();
        this.notes = null;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    public ApplicationType getApplicationType() {
        return (applicationType != null) ? ApplicationType.valueOf(applicationType) : null;

    }

    public void setApplicationType(ApplicationType applicationType) {
        this.applicationType = (applicationType != null) ? applicationType.name() : null;
    }

    public Status getStatus() {
        return (status != null) ? Status.valueOf(status) : null;
    }

    public void setStatus(Status status) {
        this.status = (status != null) ? status.name() : null;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public enum Status {
        UNSIGNED, SIGNED
    }
}
