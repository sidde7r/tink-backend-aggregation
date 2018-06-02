package se.tink.backend.common.repository.mysql.main;

import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;
import se.tink.backend.core.UserDevice;

public interface UserDeviceRepository extends PagingAndSortingRepository<UserDevice, Long>, UserDeviceRepositoryCustom {
    UserDevice findOneByUserIdAndDeviceId(String userId, String deviceId);

    List<UserDevice> findByUserId(String userId);
}
