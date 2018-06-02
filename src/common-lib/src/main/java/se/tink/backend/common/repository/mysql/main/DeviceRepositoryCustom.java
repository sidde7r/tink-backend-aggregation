package se.tink.backend.common.repository.mysql.main;

public interface DeviceRepositoryCustom {
    void deleteByUserId(String userId);

    void deleteByNotificationTokenAndNotDeviceToken(String notificationToken, String deviceToken);

    void deleteByDeviceToken(String deviceToken);

    void deleteByDeviceTokenAndNotUserId(String deviceToken, String userId);

    void deleteByNotificationToken(String notificationToken);
}
