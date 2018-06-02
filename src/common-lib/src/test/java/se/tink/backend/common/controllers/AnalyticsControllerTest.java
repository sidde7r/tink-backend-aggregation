package se.tink.backend.common.controllers;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.common.tracking.EventTracker;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(JUnitParamsRunner.class)
public class AnalyticsControllerTest {

    @Test
    @Parameters({
            "Person Har Ändrat Personnummer",
            "Person Emigrerad",
            "Person Skyddad Identitet"
    })
    public void dontTrackNameForSpecialCases(String name) {
        EventTracker tracker = mock(EventTracker.class);
        AnalyticsController controller = new AnalyticsController(tracker);

        User user = getMockedUserWithName(name);
        Map<String, Object> properties = Maps.newHashMap();

        controller.trackUserProperties(user, properties);

        verify(tracker).trackUserProperties(argThat(e -> !e.getProperties().containsKey(EventTracker.Properties.NAME)));
    }

    @Test
    @Parameters({
            "Person Person",
            "Test Testsson",
            "Qwerty"
    })
    public void trackNameForNormalCases(String name) {
        EventTracker tracker = mock(EventTracker.class);
        AnalyticsController controller = new AnalyticsController(tracker);

        User user = getMockedUserWithName(name);
        Map<String, Object> properties = Maps.newHashMap();

        controller.trackUserProperties(user, properties);

        verify(tracker).trackUserProperties(
                argThat(e -> Objects.equals(e.getProperties().get(EventTracker.Properties.NAME), name)));
    }

    private static User getMockedUserWithName(String name) {

        UserProfile profile = mock(UserProfile.class);
        when(profile.getName()).thenReturn(name);

        User user = mock(User.class);
        when(user.getProfile()).thenReturn(profile);

        return user;
    }
}
