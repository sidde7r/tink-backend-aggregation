package se.tink.backend.core;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import se.tink.backend.serialization.TypeReferences;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class DeviceConfiguration {
    private UUID deviceId;
    private List<String> featureFlags;
    private List<Market> markets;
    private DeviceOrigin origin;

    public DeviceConfiguration(List<String> featureFlags, List<Market> markets) {
        this.featureFlags = featureFlags;
        this.markets = markets;
    }

    public DeviceConfiguration() {
        this(Collections.emptyList(), Collections.emptyList());
    }

    public DeviceConfiguration(UUID deviceId, DeviceOrigin origin) {
        this();
        this.deviceId = deviceId;
        this.origin = origin;
    }

    public DeviceConfiguration(DeviceConfigurationEntity entity) {
        this.deviceId = UUIDUtils.fromTinkUUID(entity.getDeviceId());
        this.featureFlags = SerializationUtils
                .deserializeFromString(entity.getFeatureFlagsSerialized(), TypeReferences.LIST_OF_STRINGS);
        this.origin = Optional.ofNullable(entity.getOriginSerialized())
                .map(origin -> SerializationUtils.deserializeFromString(origin, DeviceOrigin.class)).orElse(null);
        this.markets = Collections.emptyList();
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(UUID deviceId) {
        this.deviceId = deviceId;
    }

    public List<String> getFeatureFlags() {
        return featureFlags;
    }

    public void setFeatureFlags(List<String> featureFlags) {
        this.featureFlags = featureFlags;
    }

    public Optional<DeviceOrigin> getOrigin() {
        return Optional.ofNullable(origin);
    }

    public void setOrigin(DeviceOrigin origin) {
        this.origin = origin;
    }

    public List<Market> getMarkets() {
        return markets;
    }

    public void setMarkets(List<Market> markets) {
        this.markets = markets;
    }

}
