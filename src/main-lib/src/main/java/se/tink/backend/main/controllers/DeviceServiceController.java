package se.tink.backend.main.controllers;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import se.tink.backend.common.dao.DeviceConfigurationDao;
import se.tink.backend.common.repository.mysql.main.DeviceRepository;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.core.Device;
import se.tink.backend.core.DeviceConfiguration;
import se.tink.backend.core.Market;
import se.tink.backend.core.UserOrigin;
import se.tink.backend.main.utils.OnboardingSelector;
import se.tink.backend.rpc.DeregisterUserPushTokenCommand;
import se.tink.backend.rpc.GetDeviceConfigurationCommand;
import se.tink.backend.rpc.RegisterUserPushTokenCommand;
import se.tink.backend.rpc.SetOriginCommand;
import se.tink.backend.utils.BeanUtils;

public class DeviceServiceController {
    private static final String DEFAULT_APPID = "se.tink.frontend.mobile";
    private static final LogUtils log = new LogUtils(DeviceServiceController.class);

    private final DeviceConfigurationDao deviceConfigurationDao;
    private final DeviceRepository deviceRepository;
    private final MarketServiceController marketServiceController;
    private final OnboardingSelector onboardingSelector;

    @Inject
    public DeviceServiceController(DeviceConfigurationDao deviceConfigurationDao, DeviceRepository deviceRepository,
            MarketServiceController marketServiceController, OnboardingSelector onboardingSelector) {
        this.deviceConfigurationDao = deviceConfigurationDao;
        this.deviceRepository = deviceRepository;
        this.marketServiceController = marketServiceController;
        this.onboardingSelector = onboardingSelector;
    }

    public Device updateDevice(String userId, String deviceToken, Device device) {
        // If there's no AppId, use a default set.

        if (Strings.isNullOrEmpty(device.getAppId())) {
            device.setAppId(DEFAULT_APPID);
        }

        // Find device by device token

        Device existingDevice = deviceRepository.findByUserIdAndDeviceToken(userId, deviceToken);

        if (existingDevice == null) {
            // For legacy reasons, try find the device by it's notification token.

            existingDevice = deviceRepository
                    .findByUserIdAndNotificationToken(userId, device.getNotificationToken());

            if (existingDevice != null) {
                existingDevice.setDeviceToken(deviceToken);
            }
        }

        if (existingDevice == null) {
            // Create a new device.

            existingDevice = new Device();

            BeanUtils.copyCreatableProperties(device, existingDevice);
        } else {
            // Modify existing device.

            BeanUtils.copyModifiableProperties(device, existingDevice);

            existingDevice.setUpdated(new Date());
        }

        existingDevice.setDeviceToken(deviceToken);
        existingDevice.setUserId(userId);

        // Clean up any other devices with the same notification token or different user id.

        deviceRepository.deleteByDeviceTokenAndNotUserId(deviceToken, userId);
        deviceRepository.deleteByNotificationTokenAndNotDeviceToken(existingDevice.getNotificationToken(),
                existingDevice.getDeviceToken());

        // Save the device.

        return deviceRepository.save(existingDevice);
    }

    public void deleteDevice(String deviceToken) {
        deviceRepository.deleteByDeviceToken(deviceToken);

        // TODO: This is a leftover from the days where we didn't register device tokens, so we
        // need to clean up device tokens as well. The conversion was done 2016-07-01, so remove
        // this after 2016-12-31 or something.

        deviceRepository.deleteByNotificationToken(deviceToken);
    }

    public void deleteDevice(String userId, String deviceToken) {
        List<Device> devices = listDevices(userId);

        if (!devices.stream().map(Device::getDeviceToken).collect(Collectors.toList()).contains(deviceToken)) {
            throw new IllegalArgumentException();
        }

        deleteDevice(deviceToken);
    }

    public List<Device> listDevices(String userId) {
        return deviceRepository.findByUserId(userId);
    }

    public DeviceConfiguration getConfiguration(UUID deviceId, String desiredMarket) {
        DeviceConfiguration deviceConfiguration = getOrCreateDeviceConfiguration(deviceId, desiredMarket, true);
        List<Market> markets = marketServiceController.getSuggestedMarkets(Collections.singletonList(desiredMarket));
        deviceConfiguration.setMarkets(markets);

        return deviceConfiguration;
    }

    public DeviceConfiguration getConfiguration(GetDeviceConfigurationCommand command) {
        return getConfiguration(command.getDeviceId(), command.getDesiredMarket());
    }

    public DeviceConfiguration getDeviceConfiguration(UUID deviceId) throws NoSuchElementException {
        return deviceConfigurationDao.find(deviceId).get();
    }

    private DeviceConfiguration getOrCreateDeviceConfiguration(UUID deviceId, String desiredMarket, boolean withRetry) {
        DeviceConfiguration configuration;

        Optional<DeviceConfiguration> existingConfiguration = deviceConfigurationDao.find(deviceId);
        if (existingConfiguration.isPresent()) {
            configuration = existingConfiguration.get();

            // Flags has already been generated. To keep consistency (so that the user don't get different onboarding
            // flows when restarting the app), don't re-generate them.
            if (!configuration.getFeatureFlags().isEmpty()) {
                return configuration;
            }
        } else {
            configuration = new DeviceConfiguration();
            configuration.setDeviceId(deviceId);
        }

        configuration.setFeatureFlags(getOnboardingFeatures(configuration, desiredMarket));

        try {
            return deviceConfigurationDao.save(configuration);
        } catch (DataIntegrityViolationException e) {
            // We are getting duplicate primary key exceptions if the `saveOrigin` method is called at the same time as
            // `getDeviceConfiguration` since both of them do a find and then a save on the device configuration.
            // Mitigating the issue temporary with a retry in case of constraint violation exceptions
            if (withRetry) {
                log.warn("Could not save device configuration. Retrying.");
                return getOrCreateDeviceConfiguration(deviceId, desiredMarket, false); // Only retry once
            }

            throw e;
        }
    }

    private List<String> getOnboardingFeatures(DeviceConfiguration deviceConfiguration, String desiredMarket) {
        return onboardingSelector.getOnboadingFeatures(deviceConfiguration.getOrigin(), desiredMarket);
    }

    public void setOrigin(SetOriginCommand command) {

        if (Strings.isNullOrEmpty(command.getOrigin().getServiceName())) {
            // Our current mobile clients doens't set this value but only uses AppsFlyer
            // FIXME: Require the front-end to send a service name in the device origin, and remove this!
            command.getOrigin().setServiceName(UserOrigin.SERVICE_NAME_APPSFLYER);
        }

        deviceConfigurationDao.saveOrigin(command.getDeviceId(), command.getOrigin());
    }

    public Device registerUserPushToken(RegisterUserPushTokenCommand command) {
        Device device = new Device();
        device.setUserId(command.getUserId());
        device.setNotificationToken(command.getNotificationToken());
        device.setType(command.getUserAgent().contains("iOS") ? "ios" : "android");
        device.setUserAgent(command.getUserAgent());
        device.setPublicKey(command.getNotificationPublicKey());

        if (Strings.isNullOrEmpty(command.getDeviceId())) {
            log.warn(command.getUserId(), "No device token in request headers. Using notification token instead.");
            device.setDeviceToken(command.getNotificationToken());
        } else {
            device.setDeviceToken(command.getDeviceId());
        }

        return updateDevice(command.getUserId(), device.getDeviceToken(), device);
    }

    /*
    * This method is deprecated but used by legacy clients. New clients should use DeviceServiceController#deleteDevice.
    */
    @Deprecated
    public void deregisterUserPushToken(DeregisterUserPushTokenCommand command) {
        command.getNotificationToken().ifPresent(this::deleteDevice);
        command.getDeviceId().ifPresent(this::deleteDevice);
    }
}
