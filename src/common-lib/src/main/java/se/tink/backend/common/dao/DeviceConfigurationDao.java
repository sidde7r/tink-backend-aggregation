package se.tink.backend.common.dao;

import com.google.inject.Inject;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import se.tink.backend.common.repository.mysql.main.DeviceConfigurationRepository;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.core.DeviceConfiguration;
import se.tink.backend.core.DeviceConfigurationEntity;
import se.tink.backend.core.DeviceOrigin;
import se.tink.libraries.uuid.UUIDUtils;

public class DeviceConfigurationDao {
    private static final LogUtils log = new LogUtils(DeviceConfigurationDao.class);
    private final DeviceConfigurationRepository deviceConfigurationRepository;

    @Inject
    public DeviceConfigurationDao(DeviceConfigurationRepository deviceConfigurationRepository) {
        this.deviceConfigurationRepository = deviceConfigurationRepository;
    }

    public Optional<DeviceConfiguration> find(UUID deviceId) {
        return Optional.ofNullable(
                deviceConfigurationRepository.findOne(UUIDUtils.toTinkUUID(deviceId)))
                .map(DeviceConfiguration::new);
    }

    public DeviceConfiguration save(DeviceConfiguration deviceConfiguration) {
        deviceConfigurationRepository.save(new DeviceConfigurationEntity(deviceConfiguration));
        return deviceConfiguration;
    }

    public DeviceConfiguration saveOrigin(UUID deviceId, DeviceOrigin origin) {
        return saveOrigin(deviceId, origin, true);
    }

    private DeviceConfiguration saveOrigin(UUID deviceId, DeviceOrigin origin, boolean withRetry) {
        Optional<DeviceConfiguration> deviceConfiguration = find(deviceId);

        DeviceConfiguration configurationToSave;

        if (deviceConfiguration.isPresent()) {
            configurationToSave = deviceConfiguration.get();
            configurationToSave.setOrigin(origin);
        } else {
            configurationToSave = new DeviceConfiguration(deviceId, origin);
        }

        try {
            return save(configurationToSave);
        } catch (DataIntegrityViolationException e) {
            // We are getting duplicate primary key exceptions if the `saveOrigin` method is called at the same time as
            // `getDeviceConfiguration` since both of them do a find and then a save on the device configuration.
            // Mitigating the issue temporary with a retry in case of constraint violation exceptions
            if (withRetry) {
                log.warn("Could not save user origin. Retrying.");
                return saveOrigin(deviceId, origin, false); // Only retry once
            }
            throw e;
        }
    }
}
