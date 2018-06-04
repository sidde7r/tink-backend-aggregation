package se.tink.backend.common.repository.mysql;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Objects;

import se.tink.backend.common.repository.mysql.main.UserDeviceRepository;
import se.tink.backend.core.UserDevice;

public class UserDeviceInMemoryRepository extends InMemoryJpaRepository<Long, UserDevice> implements UserDeviceRepository {

    public UserDeviceInMemoryRepository() {
        super();
    }

    public UserDeviceInMemoryRepository(List<UserDevice> initial) {
        super(FluentIterable.from(initial).uniqueIndex(device -> (long) device.getId()));
    }


    @Override
    public <S extends UserDevice> S save(S s) {
        db.put(new Long(s.getId()), s);
        return s;
    }

    @Override
    public UserDevice findOneByUserIdAndDeviceId(String userId, String deviceId) {
        for (UserDevice device : db.values()) {
            if (Objects.equals(device.getUserId(), userId) && Objects.equals(device.getDeviceId(), deviceId)) {
                return device;
            }
        }
        return null;
    }

    @Override
    public List<UserDevice> findByUserId(String userId) {
        List<UserDevice> devices = Lists.newArrayList();
        for (UserDevice device : db.values()) {
            if (Objects.equals(device.getUserId(), userId)) {
                devices.add(device);
            }
        }
        return devices;
    }

    @Override
    public void deleteByUserId(String userId) {
        List<UserDevice> devices = findByUserId(userId);
        for (UserDevice device : devices) {
            db.remove((long) device.getId());
        }
    }
}
