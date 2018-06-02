package se.tink.backend.common.repository.mysql.main;

import se.tink.backend.core.Notification;
import se.tink.backend.core.NotificationStatus;

import java.util.List;

public interface NotificationRepositoryCustom {
    void deleteByUserId(String userId);

    Notification findById(String Id);

    List<Notification> findByUserId(String userId);

    List<Notification> findAllByStatus(NotificationStatus status, String prefix);

    List<Notification> findAllByUserIdAndKey(String userId, String key);

    List<Notification> findRandomUnsent(int count);
}
