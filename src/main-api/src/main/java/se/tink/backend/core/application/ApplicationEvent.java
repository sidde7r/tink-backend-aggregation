package se.tink.backend.core.application;

import com.datastax.driver.core.utils.UUIDs;
import java.util.Date;
import java.util.UUID;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;
import se.tink.backend.core.Application;
import se.tink.backend.core.enums.ApplicationStatusKey;
import se.tink.libraries.application.ApplicationType;

@Table(value = "applications_events")
public class ApplicationEvent {
    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, ordinal = 0)
    private UUID userId;
    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 1)
    private UUID applicationId;
    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 2)
    private UUID id;
    private String applicationType;
    private String applicationStatus;
    private Date applicationUpdated;

    public ApplicationEvent() {

    }

    public ApplicationEvent(Application application) {
        this.id = UUIDs.timeBased();
        this.userId = application.getUserId();
        this.applicationId = application.getId();
        this.applicationType = application.getType().name();
        this.applicationStatus = application.getStatus().getKey().name();
        this.applicationUpdated = application.getStatus().getUpdated();
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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ApplicationType getApplicationType() {
        return applicationType != null ? ApplicationType.valueOf(applicationType) : null;
    }

    public void setApplicationType(ApplicationType applicationType) {
        this.applicationType = applicationType != null ? applicationType.name() : null;
    }

    public ApplicationStatusKey getApplicationStatus() {
        return applicationStatus != null ? ApplicationStatusKey.valueOf(applicationStatus) : null;
    }

    public void setApplicationStatus(ApplicationStatusKey applicationStatus) {
        this.applicationStatus = applicationStatus != null ? applicationStatus.name() : null;
    }

    public Date getApplicationUpdated() {
        return applicationUpdated;
    }

    public void setApplicationUpdated(Date applicationUpdated) {
        this.applicationUpdated = applicationUpdated;
    }
}
