package se.tink.backend.common.mail.monthly.summary;

import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.common.mail.monthly.summary.utils.UserDeviceUtils;
import se.tink.backend.core.UserDevice;

public class UserDeviceUtilsTest {

    @Test
    public void android_devices_should_count() {
        Assert.assertEquals(1, UserDeviceUtils.getNumberOfAndroidDevices(generateTestData()));
    }

    @Test
    public void ios_devices_should_count() {
        Assert.assertEquals(1, UserDeviceUtils.getNumberOfIosDevices(generateTestData()));
    }

    private List<UserDevice> generateTestData() {
        List<UserDevice> userDeviceList = Lists.newArrayList();

        UserDevice ios = new UserDevice();
        ios.setUserAgent("Tink Mobile/2.3.0 (iOS; 8.4, iPhone)");

        UserDevice android = new UserDevice();
        android.setUserAgent("Tink Mobile/2.2.6 (Android; 5.1.1, OnePlus ONE A2003) ");

        UserDevice web = new UserDevice();
        web.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko)");

        userDeviceList.add(ios);
        userDeviceList.add(android);
        userDeviceList.add(web);

        return userDeviceList;
    }
}
