package se.tink.backend.api;

import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.core.Device;
import se.tink.backend.core.DeviceConfigurationDto;
import se.tink.backend.core.DeviceOriginDto;
import se.tink.backend.rpc.DeviceListResponse;

public interface DeviceService {
    void updateDevice(AuthenticatedUser authenticatedUser, String deviceToken, Device device);
    void deleteDevice(AuthenticatedUser authenticatedUser, String deviceToken);
    DeviceListResponse listDevices(AuthenticatedUser authenticatedUser);
    DeviceConfigurationDto getConfiguration(String deviceId, String desiredMarket);
    void setOrigin(String deviceId, DeviceOriginDto deviceOrigin);
}
