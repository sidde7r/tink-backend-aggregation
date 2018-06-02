package se.tink.backend.common.repository.mysql.main;

import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.core.Notification;
import se.tink.backend.core.NotificationPk;

public interface NotificationRepository extends JpaRepository<Notification, NotificationPk>, NotificationRepositoryCustom {
}
