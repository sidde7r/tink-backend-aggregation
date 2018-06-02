package se.tink.backend.main.auth;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.Date;
import java.util.Objects;
import se.tink.backend.common.repository.mysql.main.UserDeviceRepository;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.main.auth.exceptions.UnauthorizedDeviceException;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.User;
import se.tink.backend.core.UserDevice;
import se.tink.backend.core.UserDeviceStatuses;

public class UserDeviceController {
    private static final LogUtils log = new LogUtils(UserDeviceController.class);

    private final UserDeviceRepository userDeviceRepository;
    private static final int USER_AGENT_MAX_LENGTH =  255;

    @Inject
    public UserDeviceController(UserDeviceRepository userDeviceRepository) {
        this.userDeviceRepository = userDeviceRepository;
    }

    public UserDevice getAndUpdateUserDeviceOrCreateNew(User user, String deviceId, String userAgent) {
        if (deviceId == null || !UUIDUtils.isValidUUIDv4(deviceId)) {
            log.warn(user.getId(), String.format("DeviceId %s is not a valid UUID.", deviceId));
            return null;
        }

        UserDevice device = userDeviceRepository.findOneByUserIdAndDeviceId(user.getId(), deviceId);

        if (userAgent != null) {
            // This cutoff introduced due to Facebook sending lots of header text.
            if (userAgent.length() > USER_AGENT_MAX_LENGTH) {
                userAgent = userAgent.substring(0, USER_AGENT_MAX_LENGTH);
            }
        }

        if (device == null) {
            device = new UserDevice();
            device.setInserted(new Date());
            device.setDeviceId(deviceId);
            device.setUserId(user.getId());
            device.setStatus(UserDeviceStatuses.UNAUTHORIZED);
            if (userAgent != null) {
                device.setUserAgent(userAgent);
            }

            try {
                userDeviceRepository.save(device);
            } catch (Exception e) {
                log.error(user.getId(), "Could not create user device, trying to re-read");
                device = userDeviceRepository.findOneByUserIdAndDeviceId(user.getId(), deviceId);
            }
        } else if (userAgent != null && device.updateIfNeeded(userAgent)) {
            device.setUpdated(new Date());

            try {
                userDeviceRepository.save(device);
            } catch (Exception e) {
                log.error(user.getId(), "Could not create user device, trying to re-read", e);
                device = userDeviceRepository.findOneByUserIdAndDeviceId(user.getId(), deviceId);
            }
        }

        return device;
    }

    public void authorizeDevice(UserDevice userDevice) {
        userDevice.setStatus(UserDeviceStatuses.AUTHORIZED);
        userDeviceRepository.save(userDevice);
    }

    public void checkIfDeviceIsAuthorized(User user, String deviceId, String userAgent)
            throws UnauthorizedDeviceException {
        if (Strings.isNullOrEmpty(deviceId)) {
            throw new UnauthorizedDeviceException("");
        }

        UserDevice userDevice = this.getAndUpdateUserDeviceOrCreateNew(user, deviceId, userAgent);
        if (userDevice == null) {
            throw new UnauthorizedDeviceException("");
        }
        if (!Objects.equals(userDevice.getStatus(), UserDeviceStatuses.AUTHORIZED)) {
            throw new UnauthorizedDeviceException("");
        }
    }
}
