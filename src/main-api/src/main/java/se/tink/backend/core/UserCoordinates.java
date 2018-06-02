package se.tink.backend.core;

import java.io.Serializable;
import java.util.UUID;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.annotation.Transient;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;
import se.tink.libraries.uuid.UUIDUtils;

@Table(value = "users_coordinates")
public class UserCoordinates implements Serializable {
    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID userId;
    private String address;
    private Double latitude;
    private Double longitude;
    private UUID areaId;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    @Transient
    public Coordinate getCoordinate() {
        return Coordinate.create(latitude, longitude);
    }

    public static UserCoordinates create(String userId, String address, Coordinate coordinate) {
        UserCoordinates ac = new UserCoordinates();
        ac.setUserId(UUIDUtils.fromTinkUUID(userId));
        ac.setAddress(address);
        ac.setLatitude(coordinate.getLatitude());
        ac.setLongitude(coordinate.getLongitude());

        return ac;
    }

    public UUID getAreaId() {
        return areaId;
    }

    public void setAreaId(UUID areaId) {
        this.areaId = areaId;
    }
}
