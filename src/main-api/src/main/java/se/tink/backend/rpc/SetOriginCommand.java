package se.tink.backend.rpc;

import java.util.Objects;
import java.util.UUID;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.core.DeviceOrigin;
import se.tink.libraries.uuid.UUIDUtils;

public class SetOriginCommand {
    private UUID deviceId;
    private DeviceOrigin origin;

    public SetOriginCommand(String deviceId, DeviceOrigin origin) {
        validate(deviceId);
        this.deviceId = UUID.fromString(deviceId);
        this.origin = origin;
    }

    private void validate(String deviceId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(deviceId));
        Preconditions.checkArgument(Objects.nonNull(UUID.fromString(deviceId)));
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public DeviceOrigin getOrigin() {
        return origin;
    }
}
