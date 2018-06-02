package se.tink.backend.common.repository.mysql.main;

import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.core.DeviceConfigurationEntity;

public interface DeviceConfigurationRepository extends JpaRepository<DeviceConfigurationEntity, String> {
}
