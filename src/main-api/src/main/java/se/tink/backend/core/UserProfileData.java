package se.tink.backend.core;

import java.util.Date;
import java.util.UUID;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

@Table(value = "users_profile_data")
public class UserProfileData {

    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private String name;
    private Date updated;
    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID userId;
    private String value;

    public String getName() {
        return name;
    }

    public Date getUpdated() {
        return updated;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getValue() {
        return value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
