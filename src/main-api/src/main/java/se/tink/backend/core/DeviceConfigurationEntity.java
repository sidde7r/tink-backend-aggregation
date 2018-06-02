package se.tink.backend.core;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Type;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Entity
@Table(name = "devices_configurations")
public class DeviceConfigurationEntity {
    @Id
    private String deviceId;
    @Type(type = "text")
    private String featureFlagsSerialized;
    @Type(type = "text")
    private String originSerialized;

    public DeviceConfigurationEntity() {
    }

    public DeviceConfigurationEntity(DeviceConfiguration configuration) {
        this.deviceId = UUIDUtils.toTinkUUID(configuration.getDeviceId());
        this.featureFlagsSerialized = SerializationUtils.serializeToString(configuration.getFeatureFlags());
        this.originSerialized = configuration.getOrigin().map(SerializationUtils::serializeToString).orElse(null);
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getOriginSerialized() {
        return originSerialized;
    }

    public void setOriginSerialized(String originSerialized) {
        this.originSerialized = originSerialized;
    }

    public String getFeatureFlagsSerialized() {
        return featureFlagsSerialized;
    }

    public void setFeatureFlagsSerialized(String featureFlagsSerialized) {
        this.featureFlagsSerialized = featureFlagsSerialized;
    }
}
