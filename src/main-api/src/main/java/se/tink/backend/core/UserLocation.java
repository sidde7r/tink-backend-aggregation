package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;
import java.util.Date;
import java.util.UUID;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

@Table(value = "users_locations")
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserLocation {
    @Creatable
    protected double accuracy;
    // date field can be dropped if we need space in Cassandra. id is a time-UUID, which contains the exact same
    // information.
    @Creatable
    protected Date date;
    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 1)
    private UUID id;
    @Creatable
    protected double latitude;
    @Creatable
    protected double longitude;
    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, ordinal = 0)
    private UUID userId;

    public UserLocation() {

    }

    public double getAccuracy() {
        return accuracy;
    }

    public Date getDate() {
        return date;
    }

    public UUID getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass()).add("date", getDate()).add("latitude", getLatitude())
                .add("longitude", getLongitude()).add("accuracy", getAccuracy()).toString();
    }
}