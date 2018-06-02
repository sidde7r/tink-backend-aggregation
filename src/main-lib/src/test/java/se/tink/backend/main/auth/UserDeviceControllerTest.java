package se.tink.backend.main.auth;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Date;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.common.repository.mysql.UserDeviceInMemoryRepository;
import se.tink.backend.core.User;
import se.tink.backend.core.UserDevice;
import se.tink.backend.core.UserDeviceStatuses;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.StringUtils;

public class UserDeviceControllerTest {

    private UserDeviceInMemoryRepository repository;

    private String deviceId1 = StringUtils.generateUUID();

    @Before
    public void setUp() {
        repository = new UserDeviceInMemoryRepository();
    }

    @Test
    public void verifyAuthorizedDeviceIsStored() {
        UserDevice device = unauthorized(1, "uid1", deviceId1);

        UserDeviceController controller = new UserDeviceController(repository);

        controller.authorizeDevice(device);

        Assert.assertTrue(repository.containsNothingButTheseIds(Sets.newHashSet(1L)));
        Assert.assertEquals(UserDeviceStatuses.AUTHORIZED, repository.findOne(1L).getStatus());
    }

    @Test
    public void verifyNothingIsReturnedWithoutDeviceId() {
        UserDeviceController controller = new UserDeviceController(repository);

        Assert.assertNull(controller.getAndUpdateUserDeviceOrCreateNew(user("uid1"), null, "userAgent"));
    }

    @Test
    public void verifyUpdatedIsProlongedOnGetAndUpdate() {
        UserDevice device = unauthorized(1, "uid1", deviceId1);
        Date initialUpdated = device.getUpdated();

        repository = new UserDeviceInMemoryRepository(Lists.newArrayList(device));
        UserDeviceController controller = new UserDeviceController(repository);

        controller.getAndUpdateUserDeviceOrCreateNew(user("uid1"), deviceId1, "userAgent");

        UserDevice stored = repository.findOne(1L);

        Assert.assertEquals(device, stored);
        Assert.assertTrue(stored.getUpdated().after(initialUpdated));
    }

    @Test
    public void verifyDeviceIsCreatedAndStoredIfItDoesNotExist() {
        UserDeviceController controller = new UserDeviceController(repository);

        UserDevice device = controller.getAndUpdateUserDeviceOrCreateNew(user("uid1"), deviceId1, "userAgent");
        UserDevice stored = repository.findOneByUserIdAndDeviceId("uid1", deviceId1);

        Assert.assertEquals(device, stored);
        Assert.assertEquals(device.getId(), stored.getId());
    }

    @Test
    public void verifyDeviceWithTooLongUserAgentIsStored() {
        UserDeviceController controller = new UserDeviceController(repository);

        String userAgent = "Mozilla/5.0 (ios) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.111 Safari/537.36 [Lots of different flags in header here... FBAN/FBIOS;FBAV/149.0.0.39.64;FBBV/79173879;FBDV/iPhone9,4;FBMD/iPhone;FBSN/iOS;FBSV/11.0.3;FBSS/3;FBCR/Tele2;FBID/phone;FBLC/sv_SE;FBOP/5;FBRV/0]";

        UserDevice device = controller.getAndUpdateUserDeviceOrCreateNew(user("uid2"), deviceId1, userAgent);
        UserDevice stored = repository.findOneByUserIdAndDeviceId("uid2", deviceId1);

        Assert.assertEquals(device, stored);
        Assert.assertEquals(device.getId(), stored.getId());
        Assert.assertEquals(userAgent.substring(0, 255), stored.getUserAgent());
    }

    private static UserDevice unauthorized(int id, String userId, String deviceId) {
        UserDevice device = new UserDevice();
        device.setUserId(userId);
        device.setDeviceId(deviceId);
        device.setId(id);
        device.setInserted(DateUtils.addMinutes(new Date(), -5));
        device.setUpdated(DateUtils.addMinutes(new Date(), -1));
        device.setStatus(UserDeviceStatuses.UNAUTHORIZED);
        return device;
    }

    private static User user(String userId) {
        User user = new User();
        user.setId(userId);
        return user;
    }
}
