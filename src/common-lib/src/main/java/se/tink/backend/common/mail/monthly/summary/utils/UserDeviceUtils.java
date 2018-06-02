package se.tink.backend.common.mail.monthly.summary.utils;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.core.UserDevice;

/**
 * Utility class for counting the number of Ios or Android devices from a list of user devices
 */
public class UserDeviceUtils {

    public static int getNumberOfAndroidDevices(List<UserDevice> userDevices) {
        return countUserDevicesByUserAgentFilter(userDevices, "android");
    }

    public static int getNumberOfIosDevices(List<UserDevice> userDevices) {
        return countUserDevicesByUserAgentFilter(userDevices, "ios");
    }

    private static int countUserDevicesByUserAgentFilter(List<UserDevice> userDevices, final String filter) {

        return Iterables.size(Iterables.filter(userDevices, userDevice -> userDevice.getUserAgent() != null && StringUtils.containsIgnoreCase(userDevice.getUserAgent(),
                filter)));
    }
}
