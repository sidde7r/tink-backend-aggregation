package se.tink.backend.common.workers.notifications.channels;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import se.tink.backend.core.Device;

public class AppIdPredicate implements Predicate<Device> {

    private final String appId;

    public AppIdPredicate(String appId) {
        this.appId = Preconditions.checkNotNull(appId);
    }

    @Override
    public boolean apply(Device input) {
        if (input == null) {
            return false;
        } else {
            return Objects.equal(input.getAppId(), appId);
        }

    }

}
