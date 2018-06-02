package se.tink.backend.core.tracking;

import java.util.UUID;

import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import com.datastax.driver.core.utils.UUIDs;

@Table(value = "tracking_sessions")
public class TrackingSession {
    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, ordinal = 1)
    private UUID id;
    private UUID userId;

    public TrackingSession() {
        this.id = UUIDs.timeBased();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
