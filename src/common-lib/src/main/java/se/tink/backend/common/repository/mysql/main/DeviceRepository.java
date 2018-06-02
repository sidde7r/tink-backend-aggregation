package se.tink.backend.common.repository.mysql.main;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.core.Device;

public interface DeviceRepository extends JpaRepository<Device, Long>, DeviceRepositoryCustom {
    List<Device> findByUserId(String userId);

    Device findByUserIdAndNotificationToken(String userId, String notificationToken);

    Device findByUserIdAndDeviceToken(String userId, String deviceToken);

    long countByUserId(String userId);
}
